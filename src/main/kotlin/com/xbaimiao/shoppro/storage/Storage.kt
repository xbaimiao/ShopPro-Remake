package com.xbaimiao.shoppro.storage

import com.xbaimiao.shoppro.item.Item
import org.bukkit.entity.Player

/**
 * 限购数据存储
 */
interface Storage {

    /**
     * 获取该玩家今日对该商品的限购计数
     */
    fun getPlayerData(player: Player, item: Item): LimitData

    /**
     * 设置该玩家今日对该商品的限购计数
     */
    fun setPlayerData(player: Player, item: Item, data: LimitData)

    /**
     * 获取全服今日对该商品的限购计数
     */
    fun getServerData(item: Item): LimitData

    /**
     * 设置全服今日对该商品的限购计数
     */
    fun setServerData(item: Item, data: LimitData)

    /**
     * 给玩家和全服的限购计数各加上 [data]
     */
    fun addAmount(item: Item, player: Player, data: LimitData)

    /**
     * 清空全部限购数据
     */
    fun reset()

    fun loadPlayerData(player: Player)

    fun releasePlayerData(player: Player)

    /**
     * 关闭底层连接
     */
    fun close()

}
