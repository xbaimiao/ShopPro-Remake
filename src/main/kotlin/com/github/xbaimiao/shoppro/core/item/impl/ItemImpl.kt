package com.github.xbaimiao.shoppro.core.item.impl

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ItemLoader
import com.github.xbaimiao.shoppro.core.shop.Shop
import com.github.xbaimiao.shoppro.util.Util.replacePapi
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.platform.util.modifyLore
import java.util.*

class ItemImpl(
    materialString: String,
    override val lore: List<String>,
    override val name: String,
    override val key: Char,
    override val vanilla: Boolean,
    override val commands: List<String>,
    override val shop: Shop,
    override val enableRight: Boolean
) : Item {

    override lateinit var material: Material

    var custom: Int? = null
    var head: String? = null

    init {
        if (materialString.startsWith("HEAD:")) {
            material = XMaterial.PLAYER_HEAD.parseMaterial()!!
            head = materialString.substring(5)
        } else if (materialString.startsWith("IA:")) {
            materialString.split(":").let { strings ->
                material = XMaterial.matchXMaterial(strings[1]).get().parseMaterial()!!
                custom = strings[2].toInt()
            }
        } else {
            material = XMaterial.matchXMaterial(materialString).get().parseMaterial()!!
        }
    }

    override fun isCommodity(): Boolean {
        return false
    }

    override fun buildItem(player: Player): ItemStack {
        return com.github.xbaimiao.shoppro.util.buildItem(material) {
            this.name = this@ItemImpl.name
            this.lore.addAll(this@ItemImpl.lore)
            custom?.let { customModelData = it }
            head?.let {
                skullTexture = com.github.xbaimiao.shoppro.util.ItemBuilder.SkullTexture(it, UUID.randomUUID())
            }
        }
    }

    override fun update(player: Player): ItemStack {
        val item = buildItem(player).modifyLore {
            val newLore = ArrayList<String>()
            for (line in this) {
                newLore.add(line.replacePapi(player))
            }
            this.clear()
            this.addAll(newLore)
        }
        return item
    }

    companion object : ItemLoader() {

        override var prefix: String? = null

        override fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item {
            return ItemImpl(
                section.getString("material")!!,
                section.getStringList("lore").colored(),
                section.getString("name")!!.colored(),
                char,
                section.getBoolean("vanilla", true),
                section.getStringList("commands"),
                shop,
                section.getBoolean("right_click", true)
            )
        }
    }

}
