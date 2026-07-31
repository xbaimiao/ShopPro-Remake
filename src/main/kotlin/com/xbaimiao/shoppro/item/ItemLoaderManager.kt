package com.xbaimiao.shoppro.item

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.shoppro.item.impl.CraftEngineShopItem
import com.xbaimiao.shoppro.item.impl.HeadShopItem
import com.xbaimiao.shoppro.item.impl.MythicShopItem
import com.xbaimiao.shoppro.item.impl.NeigeShopItem
import com.xbaimiao.shoppro.item.impl.VanillaShopItem
import com.xbaimiao.shoppro.shop.Shop
import com.xbaimiao.shoppro.util.modifyLore
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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

    /**
     * 按 material 来源统一解析商品和非商品
     *
     * 非商品先复用对应来源的加载器构建图标, 再包装成不可交易的装饰品。
     */
    fun fromSection(
        char: Char,
        section: ConfigurationSection,
        shop: Shop,
        isCommodity: Boolean,
    ): Item {
        val material = section.getString("material")
            ?: error("缺少 material 配置")
        val loader = matchLoader(material) ?: vanillaLoader()
        val item = loader.fromSection(char, section, shop)
        return if (isCommodity) item else item.asNonCommodityItem()
    }

    /** 原版商品加载器, 无前缀匹配时的兜底 */
    fun vanillaLoader(): ItemLoader = VanillaShopItem

}

/**
 * 将任意来源加载出的物品包装为不可交易的非商品
 *
 * 图标仍由来源加载器创建，因此原版、HEAD、CraftEngine、MythicMobs 和 NeigeItems
 * 都能复用各自的图标解析逻辑。
 */
private fun Item.asNonCommodityItem(): Item {
    val source = this
    return object : Item by source {

        override fun isCommodity(): Boolean = false

        /** 非商品只替换展示文本中的 PAPI 变量，不执行商品价格和限购计算 */
        override fun update(player: Player): ItemStack {
            return source.createIcon(player).modifyLore {
                val replaced = map { line -> line.replacePlaceholder(player) }
                clear()
                addAll(replaced)
            }
        }

    }
}
