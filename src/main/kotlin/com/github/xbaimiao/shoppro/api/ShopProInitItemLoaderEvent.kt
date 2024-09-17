package com.github.xbaimiao.shoppro.api

import com.github.xbaimiao.shoppro.ShopPro
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import taboolib.platform.type.BukkitProxyEvent

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

}
