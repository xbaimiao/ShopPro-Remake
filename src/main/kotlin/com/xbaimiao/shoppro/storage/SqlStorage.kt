package com.xbaimiao.shoppro.storage

import com.xbaimiao.easylib.database.Ormlite
import com.xbaimiao.easylib.database.dsl.wrapper.select
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.shoppro.ShopPro
import com.xbaimiao.shoppro.item.Item
import com.xbaimiao.shoppro.storage.table.PlayerTable
import com.xbaimiao.shoppro.storage.table.ServerTable
import org.bukkit.entity.Player
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于 ORMLite 的限购数据存储
 */
abstract class SqlStorage(private val ormlite: Ormlite) : Storage {

    private val playerDao = ormlite.createDao(PlayerTable::class.java)!!
    private val serverDao = ormlite.createDao(ServerTable::class.java)!!

    /**
     * 在线玩家的限购数据缓存, 避免每次点击都查库
     */
    private val playerCache = ConcurrentHashMap<UUID, ConcurrentHashMap<String, LimitData>>()

    private val resetTask = submit(async = true, delay = 20, period = 20) {
        val currentDay = LocalDate.now().toEpochDay()
        if (currentDay != ShopPro.inst.config.getLong("internal.last-reset-day")) {
            reset()
            ShopPro.inst.config.set("internal.last-reset-day", currentDay)
            ShopPro.inst.saveConfig()
        }
    }

    /**
     * 商品在数据库里的唯一标识
     *
     * 只用商店名和字符位, 不掺材质和模型数据
     * 否则改一次商品材质就会把玩家已有的限购记录全部作废
     */
    private fun Item.toStorageKey(): String {
        return "${shop.getName()}-$key"
    }

    override fun reset() {
        playerDao.deleteBuilder().delete()
        serverDao.deleteBuilder().delete()
        playerCache.values.forEach { it.clear() }
    }

    override fun getPlayerData(player: Player, item: Item): LimitData {
        val cache = playerCache[player.uniqueId] ?: error("玩家 ${player.name} 的数据缓存未加载")
        val key = item.toStorageKey()
        cache[key]?.let { return it }

        val stored = playerDao.select {
            PlayerTable::itemKey eq key
            PlayerTable::user eq player.uniqueId.toString()
        }?.let { LimitData.parse(it.data) } ?: LimitData.EMPTY

        cache[key] = stored
        return stored
    }

    override fun setPlayerData(player: Player, item: Item, data: LimitData) {
        val cache = playerCache[player.uniqueId] ?: error("玩家 ${player.name} 的数据缓存未加载")
        val key = item.toStorageKey()
        // 先写缓存, 读路径以缓存为准, 落库放到异步
        cache[key] = data

        submit(async = true) {
            val old = playerDao.select {
                PlayerTable::itemKey eq key
                PlayerTable::user eq player.uniqueId.toString()
            }
            if (old != null) {
                old.data = data.toString()
                playerDao.update(old)
            } else {
                playerDao.create(PlayerTable().apply {
                    itemKey = key
                    this.data = data.toString()
                    user = player.uniqueId.toString()
                })
            }
        }
    }

    override fun getServerData(item: Item): LimitData {
        val stored = serverDao.select {
            ServerTable::itemKey eq item.toStorageKey()
        } ?: return LimitData.EMPTY
        return LimitData.parse(stored.data)
    }

    /**
     * 全服限购数据不做本地缓存, 每次都读写数据库
     *
     * 这样多个服务器共用同一个 MySQL 时全服限额才是真的全服
     * 代价是这里的读写都在主线程同步进行
     */
    override fun setServerData(item: Item, data: LimitData) {
        val key = item.toStorageKey()
        val old = serverDao.select { ServerTable::itemKey eq key }
        if (old != null) {
            old.data = data.toString()
            serverDao.update(old)
        } else {
            serverDao.create(ServerTable().apply {
                itemKey = key
                this.data = data.toString()
            })
        }
    }

    override fun addAmount(item: Item, player: Player, data: LimitData) {
        setPlayerData(player, item, getPlayerData(player, item) + data)
        setServerData(item, getServerData(item) + data)
    }

    override fun loadPlayerData(player: Player) {
        playerCache[player.uniqueId] = ConcurrentHashMap()
    }

    override fun releasePlayerData(player: Player) {
        playerCache.remove(player.uniqueId)
    }

    override fun close() {
        resetTask.cancel()
        ormlite.close()
    }

}
