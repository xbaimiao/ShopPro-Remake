package com.github.xbaimiao.shoppro.core.item

import com.github.xbaimiao.shoppro.ShopPro
import com.github.xbaimiao.shoppro.core.shop.Shop
import com.github.xbaimiao.shoppro.core.vault.Currency
import com.github.xbaimiao.shoppro.util.Util.format
import com.github.xbaimiao.shoppro.util.Util.howManyItems
import com.github.xbaimiao.shoppro.util.Util.replacePapi
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.modifyLore

abstract class ShopItem(
    override val key: Char,
    override val material: Material,
    override val name: String,
    override val lore: List<String>,
    override val vanilla: Boolean,
    override val commands: List<String>,
    override val shop: Shop,
    override val conditionScript: String?,
    override val conditionIcon: Material?,
    override val conditionLore: List<String>,
    override val conditionName: String?,
    val price: Double,
    val limitServer: Long,
    private val limitPlayer: Long,
    private val limitPermissionMap: Map<String, Long>,
    val currency: Currency,
    override val enableRight: Boolean,
    val data: Int,
) : Item, KetherCondition {

    constructor(itemSetting: ItemSetting) : this(
        itemSetting.key,
        itemSetting.material,
        itemSetting.name,
        itemSetting.lore,
        itemSetting.vanilla,
        itemSetting.commands,
        itemSetting.shop,
        itemSetting.script,
        itemSetting.conditionIcon,
        itemSetting.conditionLore,
        itemSetting.conditionName,
        itemSetting.price,
        itemSetting.limitServer,
        itemSetting.limitPlayer,
        itemSetting.limitPermissionMap,
        itemSetting.currency,
        itemSetting.enableRight,
        itemSetting.data
    )

    class ItemSetting(
        val key: Char,
        val material: Material,
        val name: String,
        val lore: List<String>,
        val vanilla: Boolean,
        val commands: List<String>,
        val shop: Shop,
        val script: String?,
        val conditionIcon: Material?,
        val conditionLore: List<String>,
        val conditionName: String?,
        val price: Double,
        val limitServer: Long,
        val limitPlayer: Long,
        val limitPermissionMap: Map<String, Long>,
        var currency: Currency,
        val enableRight: Boolean,
        val data: Int,
    )

    override fun isCommodity(): Boolean {
        return true
    }

    /**
     * 构建发送给玩家的物品
     */
    abstract fun vanillaItem(player: Player): ItemStack

    abstract fun equal(itemStack: ItemStack): Boolean

    fun isLimit(): Boolean {
        return limitPlayer != 0L && limitServer != 0L
    }

    fun getLimitPlayer(player: Player): Long {
        return limitPermissionMap.filter { player.hasPermission(it.key) }.maxOfOrNull { it.value } ?: limitPlayer
    }

    override fun update(player: Player): ItemStack {
        val playerLimit = if (shop.getType() == Shop.ShopType.BUY) {
            ShopPro.database.getPlayerAlreadyData(player, this).buy
        } else {
            ShopPro.database.getPlayerAlreadyData(player, this).sell
        }
        val serverLimit = if (shop.getType() == Shop.ShopType.BUY) {
            ShopPro.database.getServerAlreadyData(this).buy
        } else {
            ShopPro.database.getServerAlreadyData(this).sell
        }
        val item = buildItem(player).modifyLore {
            val newLore = ArrayList<String>()
            for (line in this) {
                var newLine = line.replace("\${name}", name)
                    .replace("\${price}", price.toString())
                    .replace("\${money}", currency.getMoney(player).toString())
                    .replace("\${price64}", (price * 64).toString())
                    .replace("\${limit}", getLimitPlayer(player).toString())
                    .replace("\${allLimit}", limitServer.toString())
                    .replace("\${limit-player}", (getLimitPlayer(player) - playerLimit).toString())
                    .replace("\${limit-server}", (limitServer - serverLimit).toString())
                if (shop.getType() == Shop.ShopType.SELL) {
                    val priceAll = (player.inventory.howManyItems {
                        this@ShopItem.equal(it)
                    } * price).format()
                    newLine = newLine.replace("\${priceAll}", priceAll.toString())
                }
                newLore.add(newLine.replacePapi(player))
            }
            this.clear()
            this.addAll(newLore)
        }
        return item
    }

}
