package com.github.xbaimiao.shoppro.core.item.impl

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.github.xbaimiao.shoppro.core.shop.Shop
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author 小白
 * @date 2023/5/15 17:59
 **/
class MythicItem(
    private val itemStack: ItemStack,
    itemSetting: ItemSetting
) : VanillaShopItem(itemSetting) {

    override val material: Material
        get() = itemStack.type

    override fun vanillaItem(player: Player): ItemStack {
        return itemStack
    }

    override fun equal(itemStack: ItemStack): Boolean {
        return itemStack.isSimilar(this.itemStack)
    }

    companion object : ItemLoader() {

        override val prefix: String = "MM"

        override fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            if (Bukkit.getPluginManager().plugins.find { it.name.lowercase() == "MythicMobs".lowercase() } == null) {
                error("MythicMobs Plugin not found")
            }
            val mmID = section.getString("material")!!.substring(prefix.length + 1)

            val item = MythicMobs.inst().itemManager.getItem(mmID) ?: error("MythicMobs $mmID not found")
            if (!item.isPresent) {
                error("MythicMobs $mmID not found")
            }
            return MythicItem(BukkitAdapter.adapt(item.get().generateItemStack(1)), section.toItemSetting(char, shop))
        }

        override fun parseToMaterial(section: ConfigurationSection): Material {
            return Material.AIR
        }

    }

}