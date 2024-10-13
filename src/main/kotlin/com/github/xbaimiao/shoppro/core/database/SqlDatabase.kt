package com.github.xbaimiao.shoppro.core.database

import com.github.xbaimiao.shoppro.ShopPro
import com.github.xbaimiao.shoppro.core.database.dao.PlayerTable
import com.github.xbaimiao.shoppro.core.database.dao.ServerTable
import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ShopItem
import com.github.xbaimiao.shoppro.core.item.impl.ItemsAdderShopItem
import com.xbaimiao.easylib.database.Ormlite
import com.xbaimiao.easylib.database.dsl.wrapper.select
import com.xbaimiao.easylib.util.submit
import org.bukkit.entity.Player
import java.time.LocalDate

abstract class SqlDatabase(ormlite: Ormlite) : Database {

    val playerDao = ormlite.createDao(PlayerTable::class.java)!!
    val serverDao = ormlite.createDao(ServerTable::class.java)!!

    private val playerAlreadyDataCache = HashMap<String, HashMap<String, LimitData>>()

    init {
        submit(async = true, delay = 20, period = 20) {
            val currentDay = LocalDate.now().toEpochDay()
            if (currentDay != ShopPro.inst.config.getLong("date")) {
                reset()
                ShopPro.inst.config.set("date", currentDay)
                ShopPro.inst.saveConfig()
            }
        }
    }

    override fun reset() {
        playerDao.deleteBuilder().delete()
        serverDao.deleteBuilder().delete()
    }

    override fun getServerAlreadyData(item: Item): LimitData {
        val queryBuilder = serverDao.queryBuilder()
        val where = queryBuilder.where().eq("item-key", item.toCacheKey())
        val first = where.queryForFirst()
        if (first != null) {
            return LimitData.formString(first.data)
        }
        return LimitData.ofNull()
    }

    private fun Item.toCacheKey(): String {
        val item = this
        return "${item.key}-${item.material}-${if (item is ItemsAdderShopItem) item.custom else 0}-${if (item is ShopItem) item.data else 0}"
    }

    override fun getPlayerAlreadyData(player: Player, item: Item): LimitData {
        val cache = playerAlreadyDataCache[player.name] ?: error("玩家 ${player.name} 数据缓存未加载")

        val cacheKey = item.toCacheKey()
        if (cache.containsKey(cacheKey)) {
            return cache[cacheKey]!!
        }

        val databaseLimitData = playerDao.select {
            PlayerTable::itemKey eq item.toCacheKey()
            PlayerTable::user eq player.uniqueId.toString()
        }?.let { LimitData.formString(it.data) } ?: LimitData.ofNull()

        cache[cacheKey] = databaseLimitData
        return databaseLimitData
    }

    override fun setPlayerAlreadyData(player: Player, item: Item, amount: LimitData) {
        val cache = playerAlreadyDataCache[player.name] ?: error("玩家 ${player.name} 数据缓存未加载")
        val cacheKey = item.toCacheKey()
        cache[cacheKey] = amount

        submit(async = true) {
            val old = playerDao.select {
                PlayerTable::itemKey eq item.toCacheKey()
                PlayerTable::user eq player.uniqueId.toString()
            }
            if (old != null) {
                old.data = amount.toString()
                playerDao.update(old)
            } else {
                playerDao.create(PlayerTable().apply {
                    itemKey = item.toCacheKey()
                    data = amount.toString()
                    user = player.uniqueId.toString()
                })
            }
        }
    }

    override fun addAmount(item: Item, player: Player, amount: LimitData) {
        setPlayerAlreadyData(player, item, amount.add(getPlayerAlreadyData(player, item)))
        setServerAlreadyData(item, amount.add(getServerAlreadyData(item)))
    }

    override fun setServerAlreadyData(item: Item, amount: LimitData) {
        val old = serverDao.queryForEq("item-key", item.toCacheKey())?.firstOrNull()
        if (old != null) {
            old.data = amount.toString()
            serverDao.update(old)
        } else {
            serverDao.create(ServerTable().apply {
                itemKey = item.toCacheKey()
                data = amount.toString()
            })
        }
    }

    override fun loadPlayerData(player: Player) {
        playerAlreadyDataCache[player.name] = HashMap()
    }

    override fun releasePlayerData(player: Player) {
        playerAlreadyDataCache.remove(player.name)
    }

}
