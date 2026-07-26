package com.xbaimiao.shoppro.api

import com.xbaimiao.easylib.event.BukkitProxyEvent
import com.xbaimiao.shoppro.ShopPro
import com.xbaimiao.shoppro.item.ItemLoader
import org.bukkit.event.HandlerList

/**
 * 初始化物品加载器时触发
 *
 * 第三方插件可以在这里注册自己的物品来源
 */
class ShopProInitItemLoaderEvent(val plugin: ShopPro) : BukkitProxyEvent() {

    override val allowCancelled: Boolean
        get() = false

    /**
     * 注册一个自定义物品加载器
     */
    fun addLoader(loader: ItemLoader) {
        plugin.itemLoaderManager.itemLoaders += loader
    }

    override fun getHandlers(): HandlerList = handlers

    companion object {

        @JvmField
        val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers

    }

}
