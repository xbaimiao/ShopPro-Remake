package com.xbaimiao.shoppro.item

import com.xbaimiao.shoppro.item.impl.CraftEngineShopItem
import com.xbaimiao.shoppro.item.impl.DecorationItem
import com.xbaimiao.shoppro.item.impl.HeadShopItem
import com.xbaimiao.shoppro.item.impl.MythicShopItem
import com.xbaimiao.shoppro.item.impl.NeigeShopItem
import com.xbaimiao.shoppro.item.impl.VanillaShopItem

/**
 * 管理所有 [ItemLoader]
 *
 * 第三方插件可以在 ShopProInitItemLoaderEvent 里注册自己的加载器
 */
class ItemLoaderManager {

    val itemLoaders = ArrayList<ItemLoader>()

    init {
        itemLoaders += CraftEngineShopItem
        itemLoaders += HeadShopItem
        itemLoaders += MythicShopItem
        itemLoaders += NeigeShopItem
    }

    /**
     * 按配置里的 material 前缀找到对应的加载器
     */
    fun matchLoader(material: String): ItemLoader? {
        return itemLoaders.firstOrNull { loader ->
            val prefix = loader.prefix ?: return@firstOrNull false
            material.startsWith("$prefix:")
        }
    }

    /** 原版商品加载器, 无前缀匹配时的兜底 */
    fun vanillaLoader(): ItemLoader = VanillaShopItem

    /** 装饰品加载器 */
    fun decorationLoader(): ItemLoader = DecorationItem

}
