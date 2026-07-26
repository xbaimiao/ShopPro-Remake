package com.xbaimiao.shoppro.item

import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.util.parseToMaterial
import com.xbaimiao.shoppro.currency.CurrencyType
import com.xbaimiao.shoppro.currency.VaultCurrency
import com.xbaimiao.shoppro.shop.Shop
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

/**
 * 把配置节点解析成 [Item] 的加载器
 *
 * 每种物品来源(原版/CraftEngine/MythicMobs 等)一个实现
 * 靠 [prefix] 匹配配置里 material 的前缀决定用哪个
 */
abstract class ItemLoader {

    /** material 前缀, 为 null 表示不靠前缀匹配 */
    abstract val prefix: String?

    abstract fun fromSection(char: Char, section: ConfigurationSection, shop: Shop): Item

    /**
     * 解析该商品在界面上占位用的材质
     */
    open fun parseMaterial(section: ConfigurationSection): Material {
        return section.getString("material")!!.parseToMaterial()
    }

    /**
     * 取 material 里前缀之后的部分
     *
     * 例如 CE:jzy:ruby 在 prefix 为 CE 时返回 jzy:ruby
     */
    protected fun ConfigurationSection.materialId(): String {
        val material = getString("material")!!
        val currentPrefix = prefix ?: return material
        return material.substring(currentPrefix.length + 1)
    }

    /**
     * 读取商品的通用配置
     */
    fun ConfigurationSection.toItemSetting(char: Char, shop: Shop): ShopItem.ItemSetting {
        val currency = getString("currency")?.let { CurrencyType.parse(it) } ?: VaultCurrency

        val limitPermissions = HashMap<String, Long>()
        getConfigurationSection("limit-permissions")?.let { section ->
            section.getKeys(false).forEach { permission ->
                limitPermissions[permission] = section.getLong(permission)
            }
        }

        return ShopItem.ItemSetting(
            key = char,
            material = parseMaterial(this),
            name = getString("name")!!.colored(),
            lore = getStringList("lore").colored(),
            vanilla = getBoolean("vanilla", true),
            commands = getStringList("commands"),
            shop = shop,
            enableRightClick = getBoolean("enable-right-click", true),
            conditionScript = getString("condition"),
            conditionIcon = getString("condition-icon")?.parseToMaterial(),
            conditionLore = getStringList("condition-lore").colored(),
            conditionName = getString("condition-name"),
            price = getDouble("price"),
            limitServer = getLong("limit-server", DEFAULT_LIMIT),
            limitPlayer = getLong("limit-player", DEFAULT_LIMIT),
            limitPermissions = limitPermissions,
            currency = currency,
            data = getInt("data")
        )
    }

    companion object {
        /** 未配置限购时的默认值, 等同于不限制 */
        const val DEFAULT_LIMIT = 99999999L
    }

}
