package com.github.xbaimiao.shoppro.core.item.impl

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.github.xbaimiao.shoppro.core.item.ShopItem
import com.github.xbaimiao.shoppro.core.shop.Shop
import com.xbaimiao.easylib.util.parseToMaterial
import com.xbaimiao.easylib.xseries.XMaterial
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class HeadShopItem(
    headString: String,
    itemSetting: ItemSetting,
    val item: Material,
) : ShopItem(itemSetting) {

    override val material: Material = XMaterial.PLAYER_HEAD.parseMaterial()!!

    val head: String

    init {
        head = headString.substring(5)
    }

    override fun vanillaItem(player: Player): ItemStack {
        return com.xbaimiao.easylib.util.buildItem(material) {
            skullTexture = com.xbaimiao.easylib.util.ItemBuilder.SkullTexture(head, UUID.randomUUID())
            damage = data
        }
    }

    override fun equal(itemStack: ItemStack): Boolean {
        if (data != 0 && itemStack.durability.toInt() != data) {
            return false
        }
        return itemStack.itemMeta?.hasLore() == false && itemStack.type == item
    }

    override fun buildItem(player: Player): ItemStack {
        return com.xbaimiao.easylib.util.buildItem(material) {
            this.name = this@HeadShopItem.name
            this.lore.addAll(this@HeadShopItem.lore)
            skullTexture = com.xbaimiao.easylib.util.ItemBuilder.SkullTexture(head, UUID.randomUUID())
            damage = data
        }
    }

    companion object : ItemLoader() {

        override var prefix: String? = "HEAD"

        override fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            return HeadShopItem(
                section.getString("material")!!,
                section.toItemSetting(char, shop),
                parseToMaterial(section)
            )
        }

        override fun parseToMaterial(section: ConfigurationSection): Material {
            return section.getString("item")?.parseToMaterial() ?: Material.BARRIER
        }

    }

}
