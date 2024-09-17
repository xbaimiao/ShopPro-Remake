package com.github.xbaimiao.shoppro.core.item

import com.github.xbaimiao.shoppro.core.shop.Shop
import com.github.xbaimiao.shoppro.core.vault.Currency
import com.github.xbaimiao.shoppro.core.vault.CurrencyType
import com.github.xbaimiao.shoppro.core.vault.VaultImpl
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.util.parseToMaterial
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

abstract class ItemLoader {

    abstract val prefix: String?
    abstract fun formSection(char: Char, section: ConfigurationSection, shop: Shop): Item

    open fun parseToMaterial(section: ConfigurationSection): Material {
        return section.getString("material")!!.parseToMaterial()
    }

    fun ConfigurationSection.toItemSetting(char: Char, shop: Shop): ShopItem.ItemSetting {
        var currency: Currency = VaultImpl
        this.getString("currency")?.let {
            currency = CurrencyType.formString(it).func.invoke(it)
        }
        val limitMap = HashMap<String, Long>()

        this.getConfigurationSection("limitMap")?.let { limitSection ->
            limitSection.getKeys(false).forEach { key ->
                limitMap[key] = limitSection.getLong(key)
            }
        }

        return ShopItem.ItemSetting(
            key = char,
            material = parseToMaterial(this),
            name = this.getString("name")!!.colored(),
            lore = this.getStringList("lore").colored(),
            vanilla = this.getBoolean("vanilla", true),
            commands = this.getStringList("commands"),
            data = this.getInt("data"),
            shop = shop,
            script = this.getString("condition"),
            price = this.getDouble("price"),
            limitServer = this.getLong("limit", 99999999),
            limitPlayer = this.getLong("limit-player", 99999999),
            currency = currency,
            conditionIcon = this.getString("condition-icon")?.parseToMaterial(),
            conditionLore = this.getStringList("condition-lore"),
            conditionName = this.getString("condition-name"),
            limitPermissionMap = limitMap,
            enableRight = this.getBoolean("right_click", true)
        )
    }

}
