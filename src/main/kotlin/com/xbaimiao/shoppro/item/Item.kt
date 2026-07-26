package com.xbaimiao.shoppro.item

import com.xbaimiao.easylib.bridge.player.FakeOperator
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.shoppro.shop.Shop
import com.xbaimiao.shoppro.shop.ShopManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 商店界面里的一个格子
 *
 * 分两种: 可交易的商品 [ShopItem] 和纯展示/功能的装饰品
 */
interface Item {

    /** 对应布局里的字符 */
    val key: Char

    val material: Material

    val name: String

    val lore: List<String>

    /** 是否给玩家真实的原版物品, 关闭则只执行 commands */
    val vanilla: Boolean

    val commands: List<String>

    val shop: Shop

    /** 是否允许右键批量交易 */
    val enableRightClick: Boolean

    /** 是否是可交易的商品 */
    fun isCommodity(): Boolean

    /** 构建商店界面上显示的图标 */
    fun createIcon(player: Player): ItemStack

    /** 构建展示给玩家的图标, 会处理变量替换 */
    fun update(player: Player): ItemStack

    /**
     * 执行该格子配置的命令
     *
     * @param amount 交易数量, 用于替换 {amount} 变量
     */
    fun executeCommands(player: Player, amount: Int) {
        commands.asSequence()
            .map {
                it.replace("%player%", player.name)
                    .replace("\${amount}", amount.toString())
                    .replace("{amount}", amount.toString())
            }
            .forEach { command -> dispatch(player, command) }
    }

    private fun dispatch(player: Player, command: String) {
        when {
            command.startsWith("[tell] ") -> {
                player.sendMessage(command.removePrefix("[tell] ").colored())
            }

            command.startsWith("[console] ") -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.removePrefix("[console] "))
            }

            command.startsWith("[player] ") -> {
                Bukkit.dispatchCommand(player, command.removePrefix("[player] "))
            }

            command.startsWith("[op] ") -> {
                Bukkit.dispatchCommand(FakeOperator(player), command.removePrefix("[op] "))
            }

            command.startsWith("[open] ") -> {
                val shopName = command.removePrefix("[open] ")
                ShopManager.byName(shopName)?.open(player)
            }

            command == "close" || command.startsWith("[close]") -> {
                player.closeInventory()
            }
        }
    }

}
