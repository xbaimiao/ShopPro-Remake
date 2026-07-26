package com.xbaimiao.shoppro.item.impl

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.util.ItemBuilder
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.parseToXMaterial
import com.xbaimiao.easylib.xseries.XMaterial
import com.xbaimiao.shoppro.item.Item
import com.xbaimiao.shoppro.item.ItemLoader
import com.xbaimiao.shoppro.shop.Shop
import com.xbaimiao.shoppro.util.modifyLore
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 装饰品
 *
 * 界面里不可交易的格子, 比如边框玻璃板和翻页按钮
 * 配置里 is-commodity 为 false 时使用
 */
class DecorationItem(
    materialString: String,
    override val lore: List<String>,
    override val name: String,
    override val key: Char,
    override val vanilla: Boolean,
    override val commands: List<String>,
    override val shop: Shop,
    override val enableRightClick: Boolean,
) : Item {

    override val material: Material

    /** 头颅材质值, 仅当配置写 HEAD: 前缀时有值 */
    private val headTexture: String?

    init {
        if (materialString.startsWith("$HEAD_PREFIX:")) {
            material = XMaterial.PLAYER_HEAD.parseMaterial()!!
            headTexture = materialString.removePrefix("$HEAD_PREFIX:")
        } else {
            material = materialString.parseToXMaterial().parseMaterial()!!
            headTexture = null
        }
    }

    override fun isCommodity(): Boolean = false

    override fun createIcon(player: Player): ItemStack {
        return buildItem(material) {
            name = this@DecorationItem.name
            lore.addAll(this@DecorationItem.lore)
            headTexture?.let { skullTexture = ItemBuilder.SkullTexture(it) }
        }
    }

    override fun update(player: Player): ItemStack {
        return createIcon(player).modifyLore {
            val replaced = map { it.replacePlaceholder(player) }
            clear()
            addAll(replaced)
        }
    }

    companion object : ItemLoader() {

        private const val HEAD_PREFIX = "HEAD"

        override val prefix: String? = null

        override fun fromSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            return DecorationItem(
                materialString = section.getString("material")!!,
                lore = section.getStringList("lore").colored(),
                name = section.getString("name")!!.colored(),
                key = char,
                vanilla = section.getBoolean("vanilla", true),
                commands = section.getStringList("commands"),
                shop = shop,
                enableRightClick = section.getBoolean("enable-right-click", true)
            )
        }

    }

}
