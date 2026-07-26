package com.xbaimiao.shoppro.item.impl

import com.xbaimiao.easylib.util.ItemBuilder
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.hasLore
import com.xbaimiao.easylib.util.parseToMaterial
import com.xbaimiao.easylib.xseries.XMaterial
import com.xbaimiao.shoppro.item.Item
import com.xbaimiao.shoppro.item.ItemLoader
import com.xbaimiao.shoppro.item.ShopItem
import com.xbaimiao.shoppro.shop.Shop
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 头颅商品
 *
 * 界面上显示成自定义材质的头颅, 实际收购的是 item 字段配置的材质
 * 用于给普通材质配一个好看的图标
 */
class HeadShopItem(
    private val headTexture: String,
    setting: ItemSetting,
    /** 实际交易的材质 */
    private val tradeMaterial: Material,
) : ShopItem(setting) {

    override val material: Material = XMaterial.PLAYER_HEAD.parseMaterial()!!

    override fun buildVanillaItem(player: Player): ItemStack {
        return buildItem(tradeMaterial) {
            damage = data
        }
    }

    override fun matches(itemStack: ItemStack): Boolean {
        if (data != 0 && itemStack.durability.toInt() != data) {
            return false
        }
        return itemStack.type == tradeMaterial && !itemStack.hasLore()
    }

    override fun createIcon(player: Player): ItemStack {
        return buildItem(material) {
            name = this@HeadShopItem.name
            lore.addAll(this@HeadShopItem.lore)
            skullTexture = ItemBuilder.SkullTexture(headTexture)
            damage = this@HeadShopItem.data
        }
    }

    companion object : ItemLoader() {

        override val prefix: String = "HEAD"

        override fun fromSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            return HeadShopItem(
                headTexture = section.materialId(),
                setting = section.toItemSetting(char, shop),
                tradeMaterial = parseMaterial(section)
            )
        }

        /**
         * 头颅商品交易的是 item 字段而不是 material 字段
         */
        override fun parseMaterial(section: ConfigurationSection): Material {
            return section.getString("item")?.parseToMaterial()
                ?: error("头颅商品必须配置 item 字段指明实际交易的材质")
        }

    }

}
