package com.github.xbaimiao.shoppro

import com.github.xbaimiao.shoppro.api.ShopProInitItemLoaderEvent
import com.github.xbaimiao.shoppro.core.database.Database
import com.github.xbaimiao.shoppro.core.database.MysqlDatabase
import com.github.xbaimiao.shoppro.core.database.SQLiteDatabase
import com.github.xbaimiao.shoppro.core.item.ItemLoaderManager
import com.github.xbaimiao.shoppro.core.shop.ShopManager
import com.github.xbaimiao.shoppro.core.vault.DiyCurrency
import com.github.xbaimiao.shoppro.core.vault.VaultImpl
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.ShortUUID
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.registerListener
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Suppress("unused")
class ShopPro : EasyPlugin(), Listener {

    companion object {
        val inst get() = plugin as ShopPro
    }

    lateinit var database: Database

    val itemLoaderManager = ItemLoaderManager()

    override fun enable() {
        saveDefaultConfig()
        registerListener(this)
        logger.info("${description.name} 插件启动成功 ${ShortUUID.randomShortUUID()}")
    }

    override fun active() {
        ShopProInitItemLoaderEvent(this).call()

        DiyCurrency.load()
        ShopManager.load()
        VaultImpl.startTask()

        database = if (config.getBoolean("mysql.enable")) MysqlDatabase(config.getConfigurationSection("mysql")!!) else SQLiteDatabase()

        Bukkit.getOnlinePlayers().forEach {
            database.loadPlayerData(it)
        }
    }

    override fun disable() {
        VaultImpl.flush()
    }

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        database.loadPlayerData(player)
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        val player = event.player
        database.releasePlayerData(player)
    }

    fun reload() {
        reloadConfig()
        ShopManager.shops.clear()
        DiyCurrency.load()
        ShopManager.load()

        Bukkit.getOnlinePlayers().forEach {
            database.releasePlayerData(it)
        }

        database = if (config.getBoolean("mysql.enable")) MysqlDatabase(config) else SQLiteDatabase()
        Bukkit.getOnlinePlayers().forEach {
            database.loadPlayerData(it)
        }
    }

}
