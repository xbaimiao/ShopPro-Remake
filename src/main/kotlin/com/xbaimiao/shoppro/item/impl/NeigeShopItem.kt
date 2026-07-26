package com.xbaimiao.shoppro.item.impl

import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.shoppro.item.Item
import com.xbaimiao.shoppro.item.ItemLoader
import com.xbaimiao.shoppro.item.ShopItem
import com.xbaimiao.shoppro.shop.Shop
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import pers.neige.neigeitems.manager.ItemManager

/**
 * NeigeItems 物品商品
 *
 * 配置写法 material: 'NeigeItems:物品ID'
 */
class NeigeShopItem(
    private val neigeId: String,
    private val template: ItemStack,
    setting: ItemSetting,
) : ShopItem(setting) {

    override val material: Material
        get() = template.type

    override fun buildVanillaItem(player: Player): ItemStack {
        // NI 物品支持按玩家生成, 走带 player 的重载
        return ItemManager.getItemStack(neigeId, player) ?: template.clone()
    }

    /**
     * 用 NI 自己的 isNiItem 读物品上的 NBT 标记
     *
     * 比对生成的样本物品是不可靠的, NI 物品可能带随机数据
     */
    override fun matches(itemStack: ItemStack): Boolean {
        val info = ItemManager.isNiItem(itemStack) ?: return false
        return info.id == neigeId
    }

    override fun createIcon(player: Player): ItemStack {
        return buildItem(template.clone()) {
            name = this@NeigeShopItem.name
            lore.clear()
            lore.addAll(this@NeigeShopItem.lore)
        }
    }

    companion object : ItemLoader() {

        override val prefix: String = "NeigeItems"

        override fun fromSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            if (Bukkit.getPluginManager().getPlugin("NeigeItems") == null) {
                error("未安装 NeigeItems 插件")
            }
            val neigeId = section.materialId()
            val generator = ItemManager.getItem(neigeId)
                ?: error("NeigeItems 中不存在物品 $neigeId")
            return NeigeShopItem(neigeId, generator.staticItemStack.clone(), section.toItemSetting(char, shop))
        }

        override fun parseMaterial(section: ConfigurationSection): Material {
            return Material.BARRIER
        }

    }

}
