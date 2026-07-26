package com.xbaimiao.shoppro.api

import com.xbaimiao.easylib.event.BukkitProxyEvent
import com.xbaimiao.shoppro.item.ShopItem
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

/**
 * 玩家购买商品后触发
 *
 * 该事件在交易完成后触发, 取消无效
 */
class ShopProBuyEvent(
    val item: ShopItem,
    val amount: Int,
    val player: Player,
) : BukkitProxyEvent() {

    override val allowCancelled: Boolean
        get() = false

    override fun getHandlers(): HandlerList = handlers

    companion object {

        @JvmField
        val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers

    }

}
