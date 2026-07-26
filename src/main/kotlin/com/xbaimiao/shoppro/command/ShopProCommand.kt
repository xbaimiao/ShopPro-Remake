package com.xbaimiao.shoppro.command

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.buildArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.util.CommandBody
import com.xbaimiao.easylib.util.ECommandHeader
import com.xbaimiao.shoppro.ShopPro
import com.xbaimiao.shoppro.shop.Shop
import com.xbaimiao.shoppro.shop.ShopManager
import com.xbaimiao.shoppro.shop.ShopType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@ECommandHeader("shoppro")
object ShopProCommand {

    private val shopArgNode = buildArgNode<Shop?>()
        .usage("商店")
        .compile { token -> ShopManager.names().filter { it.startsWith(token) } }
        .parse { token -> ShopManager.byName(token) }
        .build()

    @CommandBody
    val open = command<CommandSender>("open") {
        description = "打开商店"
        val shopArg = arg(shopArgNode)
        val playerArg = players("玩家(可选)", optional = true)
        exec {
            val shop = shopArg.value() ?: return@exec error("商店不存在")
            val target = playerArg.valueOrNull()

            // 给别人开商店需要额外权限
            if (target != null) {
                if (!sender.hasPermission("shoppro.command.open.admin")) {
                    sender.sendLang("shop-not-permission")
                    return@exec
                }
                shop.open(target)
                return@exec
            }

            val player = sender as? Player ?: return@exec error("控制台请指定玩家")
            if (!player.hasPermission("shoppro.command.open.${shop.getName()}")) {
                player.sendLang("shop-not-permission")
                return@exec
            }
            shop.open(player)
        }
    }

    @CommandBody
    val sellAll = command<CommandSender>("sellAll") {
        description = "出售背包内该商店收购的全部物品"
        permission = "shoppro.command.sellall"
        val shopArg = arg(shopArgNode)
        val playerArg = players("玩家(可选)", optional = true)
        exec {
            val shop = shopArg.value() ?: return@exec error("商店不存在")
            val target = playerArg.valueOrNull()

            if (target != null) {
                if (!sender.hasPermission("shoppro.command.sellall.admin")) {
                    sender.sendLang("shop-not-permission")
                    return@exec
                }
                sellAll(target, shop)
                return@exec
            }

            val player = sender as? Player ?: return@exec error("控制台请指定玩家")
            sellAll(player, shop)
        }
    }

    private fun sellAll(player: Player, shop: Shop) {
        if (shop.getType() != ShopType.SELL) {
            player.sendLang("sell-all-error")
            return
        }
        shop.sellAll(player)
    }

    @CommandBody
    val resetLimit = command<CommandSender>("resetLimit") {
        description = "重置全部限购数据"
        permission = "shoppro.resetlimit"
        exec {
            ShopPro.inst.storage.reset()
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
