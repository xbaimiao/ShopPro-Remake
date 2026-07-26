package com.xbaimiao.shoppro.item

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.expression.expression
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.warn
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 商品的解锁条件
 *
 * 条件不满足时该格子显示成另一个物品且不可交易
 */
interface ItemCondition {

    /** 条件表达式, 为 null 表示无条件 */
    val conditionScript: String?

    val conditionIcon: Material?

    val conditionLore: List<String>

    val conditionName: String?

    /**
     * 检查玩家是否满足条件
     *
     * 表达式解析失败时按不满足处理, 避免配置写错反而让所有人都能买
     */
    fun checkCondition(player: Player): Boolean {
        val script = conditionScript ?: return true
        return runCatching {
            script.replacePlaceholder(player).expression().asBoolean()
        }.getOrElse {
            warn("条件表达式 \"$script\" 解析失败: ${it.message}")
            false
        }
    }

    /**
     * 构建条件不满足时显示的物品
     */
    fun buildConditionItem(player: Player): ItemStack {
        val icon = conditionIcon ?: return ItemStack(Material.AIR)
        val displayName = conditionName ?: return ItemStack(Material.AIR)
        return buildItem(icon) {
            name = displayName.colored().replacePlaceholder(player)
            conditionLore.forEach {
                lore += it.colored().replacePlaceholder(player)
            }
        }
    }

}
