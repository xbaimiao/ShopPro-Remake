package com.github.xbaimiao.shoppro.core

import com.github.xbaimiao.shoppro.ShopPro
import com.github.xbaimiao.shoppro.core.shop.Shop
import com.github.xbaimiao.shoppro.core.shop.ShopManager
import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.buildArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.util.CommandBody
import com.xbaimiao.easylib.util.ECommandHeader
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@ECommandHeader("shoppro")
object Commands {

    val shopArgNode = buildArgNode<Shop?>()
        .usage("商店")
        .compile { token ->
            ShopManager.shops.map { it.getName() }.filter { it.startsWith(token) }
        }.parse { token ->
            ShopManager.shops.firstOrNull { it.getName() == token }
        }.build()

    @CommandBody
    val open = command<CommandSender>("open") {
        description = "打开商店"
        val shopArg = arg(shopArgNode)
        val playerArg = players("玩家(可选)", optional = true)
        exec {
            val target = playerArg.valueOrNull()
            val shop = shopArg.value() ?: return@exec error("商店不存在")
            if (target != null && sender.hasPermission("shoppro.command.open.admin")) {
                shop.open(target)
                return@exec
            }
            if (sender is Player) {
                if (!sender.hasPermission("shoppro.command.open.${shop.getName()}")) {
                    sender.sendLang("shop-not-permission")
                    return@exec
                }
                shop.open(sender as Player)
            } else {
                error("控制台请指定玩家")
            }
        }
    }

    @CommandBody
    val sellAll = command<CommandSender>("sellAll") {
        description = "出售背包内全部物品"
        permission = "shoppro.command.sellall"
        val shopArg = arg(shopArgNode)
        val playerArg = players("玩家(可选)", optional = true)
        exec {
            val target = playerArg.valueOrNull()
            val shop = shopArg.value() ?: return@exec error("商店不存在")
            if (target != null && sender.hasPermission("shoppro.command.sellall.admin")) {
                sellAll(target, shop)
                return@exec
            }
            if (sender is Player) {
                sellAll(sender as Player, shop)
            } else {
                error("只能玩家才能出售自己背包的物品 控制台请指定玩家")
            }
        }
    }

    private fun sellAll(player: Player, shop: Shop) {
        if (shop.getType() != Shop.ShopType.SELL) {
            player.sendLang("sell-all-error")
            return
        }
        shop.sellAll(player)
    }

    @CommandBody
    val resetLimit = command<CommandSender>("resetLimit") {
        description = "重置限购数据"
        permission = "shoppro.resetlimit"
        exec {
            ShopPro.inst.database.reset()
            sender.sendLang("reset")
        }
    }

    @CommandBody
    val reload = command<CommandSender>("reload") {
        description = "重载插件"
        permission = "shoppro.reload"
        exec {
            ShopPro.inst.reload()
            sender.sendLang("reload")
        }
    }

}
