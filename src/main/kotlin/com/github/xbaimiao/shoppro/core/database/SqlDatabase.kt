package com.github.xbaimiao.shoppro.core.database

import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.ShopItem
import com.github.xbaimiao.shoppro.core.item.impl.ItemsAdderShopItem
import org.bukkit.entity.Player
import taboolib.common.platform.function.submitAsync
import taboolib.module.database.Host
import taboolib.module.database.Table
import javax.sql.DataSource

abstract class SqlDatabase : Database {

    abstract val host: Host<*>
    abstract val playerTable: Table<*, *>
    abstract val serverTable: Table<*, *>
    abstract val dataSource: DataSource

    val serverTableName = "server"
    val playerTableName = "player"

    val itemKeyLine = "item-key"
    val itemMaterialLine = "item-material"
    val itemCustomLine = "item-custom"
    val dataLine = "data"

    // ServerTable 无 User
    val playerLine = "user"

    private val playerAlreadyDataCache = HashMap<String, HashMap<String, LimitData>>()

    override fun reset() {
        serverTable.workspace(dataSource) {
            executeUpdate("DELETE FROM $serverTableName;").run()
        }.run()
        playerTable.workspace(dataSource) {
            executeUpdate("DELETE FROM $playerTableName;").run()
        }.run()
    }

    override fun getServerAlreadyData(item: Item): LimitData {
        return serverTable.workspace(dataSource) {
            select {
                where {
                    itemKeyLine eq item.key.toString()
                    itemMaterialLine eq item.material.toString()
                    if (item is ItemsAdderShopItem) {
                        itemCustomLine eq item.custom
                    }
                }
            }
        }.firstOrNull {
            LimitData.formString(this.getString(dataLine))
        } ?: LimitData.ofNull()
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

        val databaseLimitData = playerTable.workspace(dataSource) {
            select {
                where {
                    playerLine eq player.uniqueId.toString()
                    itemKeyLine eq item.key.toString()
                    itemMaterialLine eq item.material.toString()
                    if (item is ItemsAdderShopItem) {
                        itemCustomLine eq item.custom
                    }
                }
            }
        }.firstOrNull {
            LimitData.formString(this.getString(dataLine))
        } ?: LimitData.ofNull()

        cache[cacheKey] = databaseLimitData
        return databaseLimitData
    }

    override fun setPlayerAlreadyData(player: Player, item: Item, amount: LimitData) {
        val cache = playerAlreadyDataCache[player.name] ?: error("玩家 ${player.name} 数据缓存未加载")
        val cacheKey = item.toCacheKey()
        cache[cacheKey] = amount

        submitAsync {
            if (playerTable.workspace(dataSource) {
                    select {
                        where {
                            playerLine eq player.uniqueId.toString()
                            itemKeyLine eq item.key.toString()
                            itemMaterialLine eq item.material.toString()
                            if (item is ItemsAdderShopItem) {
                                itemCustomLine eq item.custom
                            }
                        }
                    }
                }.find()) {
                playerTable.workspace(dataSource) {
                    update {
                        where {
                            playerLine eq player.uniqueId.toString()
                            itemKeyLine eq item.key.toString()
                            itemMaterialLine eq item.material.toString()
                            if (item is ItemsAdderShopItem) {
                                itemCustomLine eq item.custom
                            }
                        }
                        set(dataLine, amount.toString())
                    }
                }.run()
            } else {
                val custom = if (item is ItemsAdderShopItem) item.custom else 0
                playerTable.workspace(dataSource) {
                    insert {
                        value(
                            item.key.toString(),
                            item.material.toString(),
                            custom,
                            amount.toString(),
                            player.uniqueId.toString()
                        )
                    }
                }.run()
            }
        }
    }

    override fun addAmount(item: Item, player: Player, amount: LimitData) {
        setPlayerAlreadyData(player, item, amount.add(getPlayerAlreadyData(player, item)))
        setServerAlreadyData(item, amount.add(getServerAlreadyData(item)))
    }

    override fun setServerAlreadyData(item: Item, amount: LimitData) {
        if (serverTable.workspace(dataSource) {
                select {
                    where {
                        itemKeyLine eq item.key.toString()
                        itemMaterialLine eq item.material.toString()
                        if (item is ItemsAdderShopItem) {
                            itemCustomLine eq item.custom
                        }
                    }
                }
            }.find()) {
            serverTable.workspace(dataSource) {
                update {
                    where {
                        itemKeyLine eq item.key.toString()
                        itemMaterialLine eq item.material.toString()
                        if (item is ItemsAdderShopItem) {
                            itemCustomLine eq item.custom
                        }
                    }
                    set(dataLine, amount.toString())
                }
            }.run()
        } else {
            val custom = if (item is ItemsAdderShopItem) item.custom else 0
            serverTable.workspace(dataSource) {
                insert {
                    value(item.key.toString(), item.material.toString(), custom, amount.toString())
                }
            }.run()
        }
    }

    override fun loadPlayerData(player: Player) {
        playerAlreadyDataCache[player.name] = HashMap()
    }

    override fun releasePlayerData(player: Player) {
        playerAlreadyDataCache.remove(player.name)
    }

}
