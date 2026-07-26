package com.xbaimiao.shoppro.item.impl

import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.shoppro.integration.CraftEngineHook
import com.xbaimiao.shoppro.item.Item
import com.xbaimiao.shoppro.item.ItemLoader
import com.xbaimiao.shoppro.item.ShopItem
import com.xbaimiao.shoppro.shop.Shop
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * CraftEngine 物品商品
 *
 * 配置写法 material: 'CE:命名空间:路径', 例如 CE:default:ruby
 */
class CraftEngineShopItem(
    private val itemId: String,
    setting: ItemSetting,
) : ShopItem(setting) {

    /**
     * 界面占位材质
     *
     * CE 物品的真实材质要问 CE 才知道, 这里只在 CE 挂了的时候兜底用
     */
    override val material: Material = Material.BARRIER

    override fun buildVanillaItem(player: Player): ItemStack {
        // 发给玩家的是服务端侧物品, 不能用 buildDisplayItem
        return CraftEngineHook.buildItem(itemId, 1, player)
            ?: error("CraftEngine 物品 $itemId 构建失败")
    }

    override fun matches(itemStack: ItemStack): Boolean {
        return CraftEngineHook.itemId(itemStack) == itemId
    }

    override fun createIcon(player: Player): ItemStack {
        val display = CraftEngineHook.buildDisplayItem(itemId, player)
        if (display == null) {
            warn("CraftEngine 物品 $itemId 图标构建失败, 已用屏障方块占位")
            return buildItem(material) {
                name = this@CraftEngineShopItem.name
                lore.addAll(this@CraftEngineShopItem.lore)
            }
        }
        // 用 CE 生成的物品打底, 覆盖上商店配置的名称和 lore
        return buildItem(display) {
            name = this@CraftEngineShopItem.name
            lore.clear()
            lore.addAll(this@CraftEngineShopItem.lore)
        }
    }

    companion object : ItemLoader() {

        override val prefix: String = "CE"

        override fun fromSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            if (Bukkit.getPluginManager().getPlugin("CraftEngine") == null) {
                error("未安装 CraftEngine 插件")
            }
            val itemId = section.materialId()
            if (!CraftEngineHook.exists(itemId)) {
                error("CraftEngine 中不存在物品 $itemId")
            }
            return CraftEngineShopItem(itemId, section.toItemSetting(char, shop))
        }

        /**
         * CE 物品的材质由 CE 决定, 这里不解析配置里的 material
         */
        override fun parseMaterial(section: ConfigurationSection): Material {
            return Material.BARRIER
        }

    }

}
