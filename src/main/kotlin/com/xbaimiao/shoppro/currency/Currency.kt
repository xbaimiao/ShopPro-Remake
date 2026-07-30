package com.xbaimiao.shoppro.currency

import com.xbaimiao.shoppro.util.format
import com.xbaimiao.shoppro.util.toDisplayAmount
import org.bukkit.entity.Player

/**
 * 商店使用的货币
 */
interface Currency {

    /** 在交易消息和商店界面中显示的货币别名 */
    val alias: String

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

    /**
     * 按该货币实际使用的金额精度格式化, 并移除无意义的末尾零
     */
    fun formatAmount(amount: Double): String {
        return amount.format().toDisplayAmount()
    }

    /** 显示带货币别名的完整金额 */
    fun displayAmount(amount: Double): String {
        return "${formatAmount(amount)} $alias"
    }

}
