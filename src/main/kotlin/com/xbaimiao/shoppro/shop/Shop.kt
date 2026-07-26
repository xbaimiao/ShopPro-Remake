package com.xbaimiao.shoppro.shop

import com.xbaimiao.shoppro.item.Item
import org.bukkit.entity.Player

/**
 * 一个商店
 */
abstract class Shop {

    /** 界面标题 */
    abstract fun getTitle(player: Player): String

    abstract fun getType(): ShopType

    /** 商店名, 也是命令和权限里用的标识 */
    abstract fun getName(): String

    /** 出售背包内所有该商店收购的物品 */
    abstract fun sellAll(player: Player)

    abstract fun open(player: Player)

    abstract fun getItems(): Collection<Item>

}
