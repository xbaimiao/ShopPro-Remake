package com.xbaimiao.shoppro.currency

import com.xbaimiao.easylib.bridge.economy.EconomyManager
import com.xbaimiao.shoppro.ShopPro
import org.bukkit.entity.Player

/**
 * PlayerPoints 点券货币
 *
 * PlayerPoints 只支持整数, 小数部分会被截断
 */
object PointsCurrency : Currency {

    /** PlayerPoints 货币别名可在主配置中修改 */
    override val alias: String
        get() = ShopPro.inst.config.getString("currency-aliases.points")
            ?.takeIf { it.isNotBlank() }
            ?: "点券"

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

    /** PlayerPoints 只支持整数金额 */
    override fun formatAmount(amount: Double): String {
        return amount.toInt().toString()
    }

}
