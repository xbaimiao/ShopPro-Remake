package com.xbaimiao.shoppro.currency

import org.bukkit.entity.Player

/**
 * 商店使用的货币
 */
interface Currency {

    /**
     * 玩家是否有足够的货币
     */
    fun hasMoney(player: Player, amount: Double): Boolean

    /**
     * 给予玩家货币
     */
    fun giveMoney(player: Player, amount: Double)

    /**
     * 扣除玩家货币
     *
     * @return 是否扣除成功
     */
    fun takeMoney(player: Player, amount: Double): Boolean

    /**
     * 获取玩家当前货币数量
     */
    fun getMoney(player: Player): Double

}
