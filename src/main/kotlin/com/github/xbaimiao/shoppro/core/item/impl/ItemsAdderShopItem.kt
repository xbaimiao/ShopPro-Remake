package com.github.xbaimiao.shoppro.core.item.impl

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.github.xbaimiao.shoppro.core.item.ShopItem
import com.github.xbaimiao.shoppro.core.shop.Shop
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial

class ItemsAdderShopItem(
    iaMaterial: String,
    itemSetting: ItemSetting
) : ShopItem(itemSetting) {

    override val material: Material

    val custom: Int

    init {
        iaMaterial.split(":").let { strings ->
            material = XMaterial.matchXMaterial(strings[1]).get().parseMaterial()!!
            custom = strings[2].toInt()
        }
    }

    override fun equal(itemStack: ItemStack): Boolean {
        if (itemStack.hasItemMeta() && itemStack.itemMeta!!.hasCustomModelData()) {
            return itemStack.type == material && itemStack.itemMeta?.customModelData == custom
        }
        return false
    }

    override fun vanillaItem(player: Player): ItemStack {
        return com.github.xbaimiao.shoppro.util.buildItem(material) {
            this.customModelData = custom
        }
    }

    override fun buildItem(player: Player): ItemStack {
        return com.github.xbaimiao.shoppro.util.buildItem(material) {
            this.name = this@ItemsAdderShopItem.name
            this.lore.addAll(this@ItemsAdderShopItem.lore)
            this.customModelData = custom
        }
    }

    companion object : ItemLoader() {

        override var prefix: String? = "IA"

        override fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            return ItemsAdderShopItem(
                section.getString("material")!!,
                section.toItemSetting(char, shop)
            )
        }

        override fun parseToMaterial(section: ConfigurationSection): Material {
            val iaMaterial = section.getString("material")!!
            iaMaterial.split(":").let { strings ->
                return XMaterial.matchXMaterial(strings[1]).get().parseMaterial()!!
            }
        }

    }

}
