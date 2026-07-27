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

    override fun getHandlers(): HandlerList = handlerList

    companion object {

        // 这个字段不能叫 handlers
        // 父类 Event 声明了 getHandlers(), Kotlin 会为它合成同名实例属性 handlers
        // 成员作用域优先于伴生对象, 那样 getHandlers() 里的 handlers
        // 会解析成 this.getHandlers() 从而无限递归, 启动时直接 StackOverflowError
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList

    }

}
