package com.github.xbaimiao.shoppro.core.item

import com.github.xbaimiao.shoppro.util.KetherUtil
import com.github.xbaimiao.shoppro.util.Util.replacePapi
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.chat.colored

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
        return KetherUtil.instantKether(player, conditionScript!!).asBoolean(false)
    }

    fun conditionItem(player: Player): ItemStack {
        if (conditionIcon == null || conditionName == null) {
            return ItemStack(Material.AIR)
        }
        return com.github.xbaimiao.shoppro.util.buildItem(conditionIcon!!) {
            name = conditionName!!.colored().replacePapi(player)
            conditionLore.forEach {
                lore += it.colored().replacePapi(player)
            }
        }
    }

}
