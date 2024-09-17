package com.github.xbaimiao.shoppro.api

import com.github.xbaimiao.shoppro.ShopPro
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.xbaimiao.easylib.event.BukkitProxyEvent
import org.bukkit.event.HandlerList

/**
 * @author xbaimiao
 * @date 2024/6/10
 * @email owner@xbaimiao.com
 */
class ShopProInitItemLoaderEvent(val plugin: ShopPro) : BukkitProxyEvent() {

    override val allowCancelled: Boolean
        get() = false

    fun addLoader(loader: ItemLoader) {
        plugin.itemLoaderManager.itemLoaders.add(loader)
    }

    override fun getHandlers(): HandlerList {
        return BukkitProxyEvent.getHandlerList()
    }

    companion object {

        @JvmField
        val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }

    }
}
