package com.github.xbaimiao.shoppro.core.database

import com.github.xbaimiao.shoppro.core.item.Item
import org.bukkit.entity.Player

interface Database {

    /**
     * 获取该物品该玩家今日已购买多少次了
     */
    fun getPlayerAlreadyData(player: Player, item: Item): LimitData

    /**
     * 设置该物品该玩家今日已购买多少次了
     */
    fun setPlayerAlreadyData(player: Player, item: Item, amount: LimitData)

    /**
     * 获取该物品今日已购买多少次了
     */
    fun getServerAlreadyData(item: Item): LimitData

    /**
     * 设置该物品今日已购买多少次了
     */
    fun setServerAlreadyData(item: Item, amount: LimitData)

    fun addAmount(item: Item, player: Player, amount: LimitData)

    fun reset()

    fun loadPlayerData(player: Player)

    fun releasePlayerData(player: Player)

}
