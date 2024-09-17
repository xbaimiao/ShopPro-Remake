package com.github.xbaimiao.shoppro.util

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.platform.util.isNotAir
import java.text.DecimalFormat

object Util {

    val hasPapi by lazy { Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null }.also {
        info("PlaceholderAPI Hook")
    }

    fun Double.format(): Double {
        val df = DecimalFormat("#0.00")
        return df.format(this).toDouble()
    }

    fun Inventory.howManyItems(func: (item: ItemStack) -> Boolean): Int {
        var amount = 0
        for (itemStack in this) {
            if (itemStack.isNotAir()) {
                if (func.invoke(itemStack)) {
                    amount += itemStack.amount
                }
            }
        }
        return amount
    }

    fun String.replacePapi(player: Player): String {
        if (hasPapi) {
            return PlaceholderAPI.setPlaceholders(player, this)
        }
        return this
    }

}