package com.xbaimiao.shoppro.util

import com.xbaimiao.easylib.util.isNotAir
import com.xbaimiao.easylib.util.modifyMeta
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * 是否安装了 PlaceholderAPI
 */
val hasPlaceholderAPI: Boolean by lazy {
    Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
}

/**
 * 金额保留两位小数
 */
fun Double.format(): Double {
    return DecimalFormat("#0.00").format(this).toDouble()
}

/**
 * 金额显示时移除无意义的末尾零, 同时避免用科学计数法展示
 */
fun Double.toDisplayAmount(): String {
    return BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
}

/**
 * BigDecimal 金额显示时移除无意义的末尾零
 */
fun BigDecimal.toDisplayAmount(): String {
    return stripTrailingZeros().toPlainString()
}

/**
 * 统计背包内满足条件的物品总数
 */
fun Inventory.countItems(matcher: (item: ItemStack) -> Boolean): Int {
    var amount = 0
    for (itemStack in this) {
        if (itemStack.isNotAir() && matcher(itemStack)) {
            amount += itemStack.amount
        }
    }
    return amount
}

/**
 * 修改物品 lore
 */
fun ItemStack.modifyLore(apply: MutableList<String>.() -> Unit): ItemStack {
    return this.modifyMeta<ItemMeta> {
        val lore = this.lore ?: ArrayList()
        apply(lore)
        this.lore = lore
    }
}

/**
 * 按材质最大堆叠数把总量拆成若干堆
 *
 * 直接把 amount 塞进单个 ItemStack 在超过 maxStackSize 时会丢东西
 */
fun ItemStack.splitByStackSize(totalAmount: Int): List<ItemStack> {
    val maxStackSize = this.type.maxStackSize.coerceAtLeast(1)
    val result = ArrayList<ItemStack>()
    var remaining = totalAmount
    while (remaining > 0) {
        val size = minOf(remaining, maxStackSize)
        result += this.clone().also { it.amount = size }
        remaining -= size
    }
    return result
}
