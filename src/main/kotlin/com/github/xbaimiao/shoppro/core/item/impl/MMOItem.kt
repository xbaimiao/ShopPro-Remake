package com.github.xbaimiao.shoppro.core.item.impl

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.github.xbaimiao.shoppro.core.shop.Shop
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.Type
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * MMOItem
 *
 * @author xbaimiao
 * @since 2023/10/5 12:42
 */
class MMOItem(
    private val itemStack: ItemStack,
    private val type: Type,
    private val mmoitemsID: String,
    itemSetting: ItemSetting
) : VanillaShopItem(itemSetting) {

    override val material: Material
        get() = itemStack.type

    override fun vanillaItem(player: Player): ItemStack {
        return itemStack
    }

    override fun equal(itemStack: ItemStack): Boolean {
        val t = MMOItems.getType(itemStack) ?: return false
        if (t != type) return false
        val i = MMOItems.getID(itemStack) ?: return false
        return i == mmoitemsID
    }

    companion object : ItemLoader() {

        override val prefix: String = "MMO_ITEM"

        override fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            if (Bukkit.getPluginManager().getPlugin("MMOItems") == null) {
                error("MMOItems Plugin not found")
            }
            val mmoitemsData = section.getString("material")!!.substring(prefix.length + 1).split(":")
            val type = Type.get(mmoitemsData[0]) ?: error("MMOItems ${mmoitemsData[0]} not is type")
            val id = mmoitemsData[1]
            val item = MMOItems.plugin.getItem(type, id) ?: error("MMOItems $mmoitemsData 不是一个有效的物品")
            return MMOItem(
                item,
                type,
                id,
                section.toItemSetting(char, shop)
            )
        }

        override fun parseToMaterial(section: ConfigurationSection): Material {
            return Material.AIR
        }

    }

}