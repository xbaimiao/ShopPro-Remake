package com.xbaimiao.shoppro.integration

import com.xbaimiao.easylib.util.submit
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.fusangledger.api.CurrencyService
import com.xbaimiao.fusangledger.domain.ActorType
import com.xbaimiao.fusangledger.domain.BalanceChangeRequest
import com.xbaimiao.fusangledger.domain.Operation
import com.xbaimiao.fusangledger.domain.SourceType
import com.xbaimiao.fusangledger.domain.TransactionContext
import com.xbaimiao.shoppro.ShopPro
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * FusangLedger 软集成
 *
 * FusangLedger 是 softDepend, 未安装时所有方法安全降级
 * 只有拿到 CurrencyService 之后才会真正触碰它的类, 避免未安装时抛 NoClassDefFoundError
 *
 * 注意 FusangLedger 是异步初始化的, 服务注册发生在它自己的协程里,
 * 所以这里每次都重新查 ServicesManager 而不是启动时缓存一次
 */
object FusangLedgerHook {

    /** 是否安装并启用了 FusangLedger */
    private val isInstalled: Boolean by lazy {
        Bukkit.getPluginManager().getPlugin("FusangLedger") != null
    }

    /**
     * 等待写入结果的超时, 毫秒
     *
     * 扣款在主线程上等这个时长, 所以它同时也是单次购买的卡顿上限
     * 入账在异步线程上等, 不影响主线程
     */
    private val writeTimeout: Long
        get() = ShopPro.inst.config.getLong("fusang-ledger.write-timeout-ms", 500L).coerceAtLeast(500L)

    /**
     * 取 CurrencyService, 未安装或还没就绪时返回 null
     */
    private fun service(): CurrencyService? {
        if (!isInstalled) return null
        return runCatching {
            Bukkit.getServicesManager().getRegistration(CurrencyService::class.java)?.provider
        }.getOrNull()
    }

    /**
     * 该货币的小数位, 未知货币返回 null
     */
    fun scaleOf(currencyId: String): Int? {
        val service = service() ?: return null
        return runCatching { service.currency(currencyId)?.scale }.getOrNull()
    }

    /**
     * 读取 FusangLedger 货币定义中的展示名称, 服务尚未就绪时返回 null
     */
    fun displayNameOf(currencyId: String): String? {
        val service = service() ?: return null
        return runCatching { service.currency(currencyId)?.displayName }.getOrNull()
    }

    /**
     * 读缓存余额, 不阻塞
     *
     * 玩家数据还在加载中或货币不存在时返回 null
     */
    fun cachedBalance(player: Player, currencyId: String): BigDecimal? {
        val service = service() ?: return null
        return runCatching {
            service.getCachedBalance(player.uniqueId, currencyId)?.balance
        }.getOrNull()
    }

    /**
     * 扣款并等待结果
     *
     * @return 是否确定扣款成功, 超时或异常一律按失败处理
     */
    fun withdraw(player: Player, currencyId: String, amount: BigDecimal): Boolean {
        val service = service() ?: run {
            warn("FusangLedger 尚未就绪, 无法扣除货币 $currencyId")
            return false
        }
        val requestKey = "shoppro:buy:${player.uniqueId}:${UUID.randomUUID()}"
        val request = runCatching {
            buildRequest(service, player.uniqueId, currencyId, amount, requestKey, "shoppro.buy", Operation.WITHDRAW)
        }.getOrElse {
            warn("构建 FusangLedger 扣款请求失败: ${it.message}")
            return false
        }

        val result = runCatching {
            service.withdraw(request).get(writeTimeout, TimeUnit.MILLISECONDS)
        }.getOrElse {
            // 超时不代表没扣成功, 但这里只能按失败处理避免免费发货
            // requestKey 打出来便于人工对账
            warn("FusangLedger 扣款结果未知, 已按失败处理 requestKey=$requestKey: ${it.message}")
            return false
        }

        if (!result.successful) {
            // 余额不足是正常业务分支, 不打日志, 由 ShopPro 提示玩家
            if (result.code.name != "INSUFFICIENT_FUNDS") {
                warn("FusangLedger 扣款失败 requestKey=$requestKey code=${result.code.name} ${result.message ?: ""}")
            }
            return false
        }
        return true
    }

    /**
     * 待入账的出售收入
     *
     * 只存 UUID 不存 Player: FusangLedger 按 UUID 直接写库, 玩家下线后照样能入账
     */
    private data class PendingIncome(
        val playerId: UUID,
        val playerName: String,
        val currencyId: String,
        val amount: BigDecimal,
    )

    private val pending = ConcurrentLinkedQueue<PendingIncome>()

    /**
     * 出售收入入队, 不阻塞主线程
     *
     * 出售的钱迟一秒到没有正确性问题, 而阻塞主线程有:
     * sellAll 会对每种物品各调一次, 逐个阻塞能把主线程卡死
     */
    fun deposit(player: Player, currencyId: String, amount: BigDecimal) {
        pending.offer(PendingIncome(player.uniqueId, player.name, currencyId, amount))
    }

    /**
     * 启动定时结算, 每秒把队列里的收入按玩家+货币合并后异步入账
     */
    fun startTask() {
        submit(async = true, period = 20) {
            flush()
        }
    }

    /**
     * 结算入账队列
     *
     * 把同一玩家同一货币的多笔收入合并成一笔写入,
     * 玩家一次 sellAll 卖掉十几种物品也只产生一次数据库往返
     */
    fun flush() {
        if (pending.isEmpty()) {
            return
        }
        // 按 玩家+货币 合并, 玩家名只用于日志所以不参与分组
        val settled = LinkedHashMap<Pair<UUID, String>, BigDecimal>()
        val names = HashMap<UUID, String>()
        // 用 poll 逐个取出而不是 groupBy + clear
        // 后者会把两步之间新入队的收入直接丢掉
        while (true) {
            val income = pending.poll() ?: break
            val key = income.playerId to income.currencyId
            settled[key] = (settled[key] ?: BigDecimal.ZERO).add(income.amount)
            names[income.playerId] = income.playerName
        }

        settled.forEach { (key, amount) ->
            val (playerId, currencyId) = key
            if (amount.signum() > 0) {
                settleDeposit(playerId, names[playerId] ?: playerId.toString(), currencyId, amount)
            }
        }
    }

    /**
     * 真正发起入账
     *
     * 入账失败要大声报出来并带上 requestKey:
     * 出售是先扣物品再给钱, 失败意味着玩家物品没了钱也没到, 必须留痕供人工对账
     */
    private fun settleDeposit(playerId: UUID, playerName: String, currencyId: String, amount: BigDecimal) {
        val service = service() ?: run {
            warn("FusangLedger 尚未就绪, 玩家 $playerName 的 $amount $currencyId 出售收入未能入账")
            return
        }
        val requestKey = "shoppro:sell:$playerId:${UUID.randomUUID()}"
        val request = runCatching {
            buildRequest(service, playerId, currencyId, amount, requestKey, "shoppro.sell", Operation.DEPOSIT)
        }.getOrElse {
            warn("构建 FusangLedger 入账请求失败: ${it.message}")
            return
        }

        val result = runCatching {
            service.deposit(request).get(writeTimeout, TimeUnit.MILLISECONDS)
        }.getOrNull()

        if (result == null || !result.successful) {
            val detail = result?.let { "code=${it.code.name} ${it.message ?: ""}" } ?: "结果未知(超时或异常)"
            warn(
                "FusangLedger 入账失败, 玩家 $playerName 的物品已被扣除但货币未到账! " +
                    "requestKey=$requestKey currency=$currencyId amount=$amount $detail"
            )
        }
    }

    /**
     * 构造 FusangLedger 的写入请求
     *
     * TransactionContext 的 serverId 必须和 FusangLedger 本节点一致, 否则会被判为非法上下文直接拒绝
     *
     * 这里用全参数位置调用而不是命名参数: FusangLedger 的 jar 把 kotlin 重定位到了
     * 自己的命名空间, 从本工程看这些是普通 Java 类, 拿不到 Kotlin 的默认参数
     */
    private fun buildRequest(
        service: CurrencyService,
        playerId: UUID,
        currencyId: String,
        amount: BigDecimal,
        requestKey: String,
        reasonCode: String,
        operation: Operation,
    ): BalanceChangeRequest {
        val context = TransactionContext(
            requestKey,
            UUID.randomUUID(),
            ActorType.PLUGIN,
            playerId.toString(),
            SourceType.API,
            "ShopPro",
            service.serverId(),
            reasonCode,
            null,
            null,
            null,
            emptyMap()
        )
        return BalanceChangeRequest(playerId, currencyId, amount, context, operation)
    }

}
