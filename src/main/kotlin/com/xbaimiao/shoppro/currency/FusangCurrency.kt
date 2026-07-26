package com.xbaimiao.shoppro.currency

import com.xbaimiao.easylib.util.warn
import com.xbaimiao.shoppro.ShopPro
import com.xbaimiao.shoppro.integration.FusangLedgerHook
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * FusangLedger 货币
 *
 * 配置写法 currency: 'fusang:货币ID', 例如 fusang:coin
 *
 * 三种操作的线程行为不同:
 *  - 余额读取走 FusangLedger 的本地缓存, 不阻塞
 *  - 出售入账进队列, 每秒异步批量结算, 不阻塞
 *  - 购买扣款会阻塞主线程等结果, 因为 [Currency] 是同步接口,
 *    必须先知道扣款成功与否才能决定是否发货
 *
 * 扣款的阻塞时长上限见 config.yml 的 fusang-ledger.write-timeout-ms
 */
class FusangCurrency(private val currencyId: String) : Currency {

    override fun hasMoney(player: Player, amount: Double): Boolean {
        val required = align(amount) ?: return false
        val balance = FusangLedgerHook.cachedBalance(player, currencyId) ?: return false
        return balance >= required
    }

    override fun getMoney(player: Player): Double {
        return FusangLedgerHook.cachedBalance(player, currencyId)?.toDouble() ?: 0.0
    }

    override fun takeMoney(player: Player, amount: Double): Boolean {
        val aligned = align(amount) ?: run {
            warn("金额 $amount 不符合货币 $currencyId 的精度要求, 已拒绝本次扣款")
            return false
        }
        return FusangLedgerHook.withdraw(player, currencyId, aligned)
    }

    override fun giveMoney(player: Player, amount: Double) {
        val aligned = align(amount) ?: run {
            warn("金额 $amount 不符合货币 $currencyId 的精度要求, 已拒绝本次入账")
            return
        }
        FusangLedgerHook.deposit(player, currencyId, aligned)
    }

    /**
     * 把 ShopPro 的 Double 金额对齐到该货币的小数位
     *
     * FusangLedger 会拒绝小数位超过 scale 的金额, 而 ShopPro 的价格是 Double
     * 例如 scale 为 0 的货币收到 0.5 会被判为非法金额
     *
     * @return 对齐后的金额, 无法对齐或对齐后为 0 时返回 null
     */
    private fun align(amount: Double): BigDecimal? {
        if (!amount.isFinite() || amount <= 0.0) {
            return null
        }
        val scale = FusangLedgerHook.scaleOf(currencyId) ?: return null
        val aligned = BigDecimal.valueOf(amount).setScale(scale, RoundingMode.HALF_UP)
        // 对齐后归零说明价格比该货币的最小单位还小, 这种配置本身就是错的
        return aligned.takeIf { it.signum() > 0 }
    }

    companion object {

        /** 配置里引用 FusangLedger 货币的前缀 */
        const val PREFIX = "fusang"

        /**
         * 解析 fusang:货币ID
         *
         * 启动时不校验货币是否存在: FusangLedger 是异步初始化的,
         * ShopPro 加载商店时它可能还没就绪
         */
        fun parse(name: String): FusangCurrency {
            val currencyId = name.removePrefix("$PREFIX:")
            if (currencyId.isBlank()) {
                error("FusangLedger 货币写法应为 $PREFIX:货币ID, 当前值: $name")
            }
            if (!ShopPro.inst.server.pluginManager.isPluginEnabled("FusangLedger")) {
                warn("配置引用了 FusangLedger 货币 $currencyId, 但未安装或未启用 FusangLedger")
            }
            return FusangCurrency(currencyId)
        }

    }

}
