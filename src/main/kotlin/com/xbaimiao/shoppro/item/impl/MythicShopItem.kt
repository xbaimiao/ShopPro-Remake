package com.xbaimiao.shoppro.item.impl

import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.shoppro.item.Item
import com.xbaimiao.shoppro.item.ItemLoader
import com.xbaimiao.shoppro.item.ShopItem
import com.xbaimiao.shoppro.shop.Shop
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * MythicMobs 物品商品
 *
 * 配置写法 material: 'MM:物品ID'
 */
class MythicShopItem(
    private val mythicId: String,
    private val template: ItemStack,
    setting: ItemSetting,
) : ShopItem(setting) {

    override val material: Material
        get() = template.type

    override fun buildVanillaItem(player: Player): ItemStack {
        // 每次重新生成, MM 物品可能带随机词条
        val item = MythicBukkit.inst().itemManager.getItem(mythicId)
        if (item.isPresent) {
            return BukkitAdapter.adapt(item.get().generateItemStack(1))
        }
        return template.clone()
    }

    /**
     * 用 MM 自己的判定而不是 isSimilar
     *
     * isSimilar 会比对全部 NBT, 对带随机词条或耐久损耗的 MM 物品会误判成不匹配
     */
    override fun matches(itemStack: ItemStack): Boolean {
        val itemManager = MythicBukkit.inst().itemManager
        if (!itemManager.isMythicItem(itemStack)) {
            return false
        }
        return itemManager.getMythicTypeFromItem(itemStack) == mythicId
    }

    override fun createIcon(player: Player): ItemStack {
        return buildItem(template.clone()) {
            name = this@MythicShopItem.name
            lore.clear()
            lore.addAll(this@MythicShopItem.lore)
        }
    }

    companion object : ItemLoader() {

        override val prefix: String = "MM"

        override fun fromSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
                error("未安装 MythicMobs 插件")
            }
            val mythicId = section.materialId()
            val item = MythicBukkit.inst().itemManager.getItem(mythicId)
            if (!item.isPresent) {
                error("MythicMobs 中不存在物品 $mythicId")
            }
            val template = BukkitAdapter.adapt(item.get().generateItemStack(1))
            return MythicShopItem(mythicId, template, section.toItemSetting(char, shop))
        }

        override fun parseMaterial(section: ConfigurationSection): Material {
            return Material.BARRIER
        }

    }

}
