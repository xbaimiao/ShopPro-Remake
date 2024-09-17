package com.github.xbaimiao.shoppro.util

import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.isNotAir
import com.xbaimiao.easylib.util.modifyMeta
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
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

    fun ItemStack.modifyLore(apply: MutableList<String>.() -> Unit): ItemStack {
        return this.modifyMeta<ItemMeta> {
            val lore = this.lore ?: ArrayList()
            apply(lore)
            this.lore = lore
        }
    }

}
