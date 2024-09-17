package com.github.xbaimiao.shoppro.core.item.impl

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.github.xbaimiao.shoppro.core.shop.Shop
import ink.ptms.zaphkiel.Zaphkiel
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author 小白
 * @date 2023/5/15 17:59
 **/
class ZapItem(
    private val zapItem: ink.ptms.zaphkiel.api.Item,
    itemSetting: ItemSetting
) : VanillaShopItem(itemSetting) {

    override val material: Material
        get() = zapItem.buildItemStack(null).type

    override fun vanillaItem(player: Player): ItemStack {
        return zapItem.buildItemStack(player)
    }

    override fun equal(itemStack: ItemStack): Boolean {
        val item = Zaphkiel.api().getItemHandler().getItem(itemStack) ?: return false
        return item.id == zapItem.id
    }

    companion object : ItemLoader() {
        override val prefix: String = "Zaphkiel"

        override fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            if (Bukkit.getPluginManager().plugins.find { it.name.lowercase() == "Zaphkiel".lowercase() } == null) {
                error("Zaphkiel Plugin not found")
            }
            val zapId = section.getString("material")!!.substring(prefix.length + 1)
            val item = Zaphkiel.api().getItemManager().getItem(zapId) ?: error("ZapItem $zapId not found")
            return ZapItem(item, section.toItemSetting(char, shop))
        }

        override fun parseToMaterial(section: ConfigurationSection): Material {
            return Material.AIR
        }

    }

}