package com.github.xbaimiao.shoppro.core.item.impl

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.github.xbaimiao.shoppro.core.item.ShopItem
import com.github.xbaimiao.shoppro.core.shop.Shop
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.hasLore

open class VanillaShopItem(itemSetting: ItemSetting) : ShopItem(itemSetting) {

    override fun vanillaItem(player: Player): ItemStack {
        return com.github.xbaimiao.shoppro.util.buildItem(material) {
            damage = data
        }
    }

    override fun equal(itemStack: ItemStack): Boolean {
        if (data != 0 && itemStack.durability.toInt() != data) {
            return false
        }
        return itemStack.type == material && !itemStack.hasLore()
    }

    override fun buildItem(player: Player): ItemStack {
        return com.github.xbaimiao.shoppro.util.buildItem(material) {
            this.name = this@VanillaShopItem.name
            this.lore.addAll(this@VanillaShopItem.lore)
            this.damage = data
        }
    }

    companion object : ItemLoader() {

        override var prefix: String? = null

        override fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            return VanillaShopItem(section.toItemSetting(char, shop))
        }
    }

}
