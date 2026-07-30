package com.xbaimiao.shoppro.currency

import com.xbaimiao.easylib.bridge.economy.EconomyManager
import com.xbaimiao.easylib.util.EListener
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.shoppro.ShopPro
import com.xbaimiao.shoppro.util.format
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Vault 货币
 *
 * 出售收入不会立刻入账, 而是先进队列每秒合并结算一次
 * 玩家一次性卖掉几组物品时不会对经济插件发起几十次写操作
 */
@EListener
object VaultCurrency : Currency, Listener {

    /** Vault 货币别名可在主配置中修改 */
    override val alias: String
        get() = ShopPro.inst.config.getString("currency-aliases.vault")
            ?.takeIf { it.isNotBlank() }
            ?: "金币"

    private data class PendingIncome(val player: Player, val money: Double)

    private val pending = ConcurrentLinkedQueue<PendingIncome>()

    fun startTask() {
        submit(period = 20) {
            flush()
        }
    }

    /**
     * 玩家退出时立刻结清欠他的钱, 否则这笔收入会随着玩家对象一起失效
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun quit(event: PlayerQuitEvent) {
        flush(event.player.uniqueId)
    }

    /**
     * 结算队列
     *
     * @param only 只结算该玩家, 为 null 时结算所有人
     */
    fun flush(only: UUID? = null) {
        val settled = HashMap<Player, Double>()
        // 用 poll 逐个取出而不是 groupBy + clear
        // 后者会把两步之间新入队的收入直接丢掉
        val retained = ArrayList<PendingIncome>()
        while (true) {
            val income = pending.poll() ?: break
            if (only != null && income.player.uniqueId != only) {
                retained += income
                continue
            }
            settled[income.player] = (settled[income.player] ?: 0.0) + income.money
        }
        retained.forEach { pending.offer(it) }
        settled.forEach { (player, amount) ->
            if (amount > 0) {
                EconomyManager.vault.tryGive(player, amount.format())
            }
        }
    }

    override fun hasMoney(player: Player, amount: Double): Boolean {
        return getMoney(player) >= amount.format()
    }

    override fun giveMoney(player: Player, amount: Double) {
        pending.offer(PendingIncome(player, amount))
    }

    override fun takeMoney(player: Player, amount: Double): Boolean {
        return EconomyManager.vault.tryTake(player, amount.format())
    }

    override fun getMoney(player: Player): Double {
        return EconomyManager.vault[player].format()
    }

}
