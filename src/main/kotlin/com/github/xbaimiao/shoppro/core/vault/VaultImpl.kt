package com.github.xbaimiao.shoppro.core.vault

import com.github.xbaimiao.shoppro.util.Util.format
import com.xbaimiao.easylib.bridge.economy.EconomyManager
import com.xbaimiao.easylib.util.EListener
import com.xbaimiao.easylib.util.submit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentLinkedQueue

@EListener
object VaultImpl : Currency, Listener {

    private val queue = ConcurrentLinkedQueue<ValueQueue>()

    private data class ValueQueue(val player: Player, val money: Double)

    fun startTask() {
        submit(period = 20) {
            flush()
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun quit(event: PlayerQuitEvent) {
        val iterator = queue.iterator()
        var amount = 0.0
        while (iterator.hasNext()) {
            val queue = iterator.next()
            if (queue.player.uniqueId == event.player.uniqueId) {
                amount += queue.money
                iterator.remove()
            }
        }
        if (amount > 0) {
            realGiveMoney(event.player, amount)
        }
    }

    fun flush() {
        queue.groupBy { it.player }.forEach { (player, valueQueues) ->
            val amount = valueQueues.sumOf { it.money }
            realGiveMoney(player, amount)
        }
        queue.clear()
    }

    private fun realGiveMoney(player: Player, double: Double) {
        EconomyManager.vault.give(player, double.format())
    }

    override fun hasMoney(player: Player, double: Double): Boolean {
        return getMoney(player) >= double.format()
    }

    override fun giveMoney(player: Player, double: Double) {
        queue.offer(ValueQueue(player, double))
    }

    override fun takeMoney(player: Player, double: Double): Boolean {
        if (!hasMoney(player, double.format())) {
            return false
        }
        EconomyManager.vault.take(player, double.format())
        return true
    }

    override fun getMoney(player: Player): Double {
        return EconomyManager.vault[player].format()
    }

}
