package com.github.xbaimiao.shoppro.core.shop

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ShopItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class Shop {

    enum class ShopType(val string: String) {
        BUY("buy"), SELL("sell");

        companion object {
            fun formString(string: String): ShopType {
                return ShopType.values().first { it.string == string }
            }
        }

    }

    abstract fun getTitle(player: Player): String

    abstract fun getType(): ShopType

    abstract fun getName(): String

    abstract fun sellAll(player: Player)

    abstract fun open(player: Player)

    abstract fun getItems(): Collection<Item>

    abstract fun sellItem(player: Player, shopItem: ShopItem, itemStack: ItemStack): Boolean

}