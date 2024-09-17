package com.github.xbaimiao.shoppro.api

import com.github.xbaimiao.shoppro.core.item.ShopItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ShopProSellEvent(
    val item: ShopItem,
    val amount: Int,
    val player: Player
) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

}
