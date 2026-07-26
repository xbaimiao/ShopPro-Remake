package com.xbaimiao.shoppro.item.impl

import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.hasLore
import com.xbaimiao.shoppro.item.Item
import com.xbaimiao.shoppro.item.ItemLoader
import com.xbaimiao.shoppro.item.ShopItem
import com.xbaimiao.shoppro.shop.Shop
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 原版物品商品
 */
open class VanillaShopItem(setting: ItemSetting) : ShopItem(setting) {

    override fun buildVanillaItem(player: Player): ItemStack {
        return buildItem(material) {
            damage = data
        }
    }

    /**
     * 带 lore 的物品一律不收
     *
     * 否则各种插件生成的特殊物品会被当成普通材质贱卖
     */
    override fun matches(itemStack: ItemStack): Boolean {
        if (data != 0 && itemStack.durability.toInt() != data) {
            return false
        }
        return itemStack.type == material && !itemStack.hasLore()
    }

    override fun createIcon(player: Player): ItemStack {
        return buildItem(material) {
            name = this@VanillaShopItem.name
            lore.addAll(this@VanillaShopItem.lore)
            damage = this@VanillaShopItem.data
        }
    }

    companion object : ItemLoader() {

        override val prefix: String? = null

        override fun fromSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            return VanillaShopItem(section.toItemSetting(char, shop))
        }

    }

}
