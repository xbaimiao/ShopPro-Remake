package com.github.xbaimiao.shoppro.core.item

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.expression.expression
import com.xbaimiao.easylib.util.buildItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author 小白
 * @date 2023/5/10 09:43
 **/
interface KetherCondition {

    val conditionScript: String?
    val conditionIcon: Material?
    val conditionLore: List<String>
    val conditionName: String?

    fun check(player: Player): Boolean {
        if (conditionScript == null) {
            return true
        }
        return conditionScript!!.replacePlaceholder(player).expression().asBoolean()
    }

    fun conditionItem(player: Player): ItemStack {
        if (conditionIcon == null || conditionName == null) {
            return ItemStack(Material.AIR)
        }
        return buildItem(conditionIcon!!) {
            name = conditionName!!.colored().replacePlaceholder(player)
            conditionLore.forEach {
                lore += it.colored().replacePlaceholder(player)
            }
        }
    }

}
