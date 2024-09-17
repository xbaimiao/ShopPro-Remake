package com.github.xbaimiao.shoppro.core.item.impl

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.github.xbaimiao.shoppro.core.shop.Shop
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import pers.neige.neigeitems.item.ItemGenerator
import pers.neige.neigeitems.manager.ItemManager

/**
 * @author dongzh1
 * @date 2024/1/9 17:59
 **/
class NeigeItem(
    private val neigeItem: ItemGenerator,
    itemSetting: ItemSetting,
    private val neigeId: String
) : VanillaShopItem(itemSetting) {

    override val material: Material
        get() = neigeItem.staticItemStack.type

    override fun vanillaItem(player: Player): ItemStack {
        return ItemManager.getItemStack(neigeId, player)!!
    }

    override fun equal(itemStack: ItemStack): Boolean {
        val item = ItemManager.getItemStack(neigeId) ?: return false
        return ItemManager.getItemStack(neigeId) == item
    }

    companion object : ItemLoader() {
        override val prefix: String = "NeigeItems"

        override fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            if (Bukkit.getPluginManager().plugins.find { it.name.lowercase() == "NeigeItems".lowercase() } == null) {
                error("NeigeItems Plugin not found")
            }
            val neigeId = section.getString("material")!!.substring(prefix.length + 1)
            val item = ItemManager.getItem(neigeId) ?: error("NeigeItems $neigeId not found")
            return NeigeItem(item, section.toItemSetting(char, shop), neigeId)
        }

        override fun parseToMaterial(section: ConfigurationSection): Material {
            return Material.AIR
        }

    }

}
