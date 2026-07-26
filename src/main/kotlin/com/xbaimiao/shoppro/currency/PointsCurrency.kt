package com.xbaimiao.shoppro.currency

import com.xbaimiao.easylib.bridge.economy.EconomyManager
import org.bukkit.entity.Player

/**
 * PlayerPoints 点券货币
 *
 * PlayerPoints 只支持整数, 小数部分会被截断
 */
object PointsCurrency : Currency {

    override fun hasMoney(player: Player, amount: Double): Boolean {
        return EconomyManager.playerPoints.has(player.uniqueId, amount.toInt())
    }

    override fun giveMoney(player: Player, amount: Double) {
        EconomyManager.playerPoints.tryGive(player.uniqueId, amount.toInt())
    }

    override fun takeMoney(player: Player, amount: Double): Boolean {
        if (!hasMoney(player, amount)) {
            return false
        }
        return EconomyManager.playerPoints.tryTake(player.uniqueId, amount.toInt())
    }

    override fun getMoney(player: Player): Double {
        return EconomyManager.playerPoints.get(player.uniqueId).toDouble()
    }

}
