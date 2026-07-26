package com.xbaimiao.shoppro

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.registerListener
import com.xbaimiao.shoppro.api.ShopProInitItemLoaderEvent
import com.xbaimiao.shoppro.currency.CustomCurrency
import com.xbaimiao.shoppro.currency.VaultCurrency
import com.xbaimiao.shoppro.item.ItemLoaderManager
import com.xbaimiao.shoppro.shop.ShopManager
import com.xbaimiao.shoppro.storage.MysqlStorage
import com.xbaimiao.shoppro.storage.SQLiteStorage
import com.xbaimiao.shoppro.storage.Storage
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

    lateinit var storage: Storage
        private set

    val itemLoaderManager = ItemLoaderManager()

    override fun enable() {
        saveDefaultConfig()
        registerListener(this)
    }

    override fun active() {
        // 先让第三方插件注册物品加载器, 再加载商店配置
        ShopProInitItemLoaderEvent(this).call()

        storage = createStorage()
        CustomCurrency.load()
        ShopManager.load()
        VaultCurrency.startTask()

        Bukkit.getOnlinePlayers().forEach { storage.loadPlayerData(it) }
    }

    override fun disable() {
        // 把队列里没结算的出售收入发出去, 否则玩家的钱会丢
        VaultCurrency.flush()
        if (this::storage.isInitialized) {
            storage.close()
        }
    }

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        storage.loadPlayerData(event.player)
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        storage.releasePlayerData(event.player)
    }

    fun reload() {
        reloadConfig()

        VaultCurrency.flush()
        Bukkit.getOnlinePlayers().forEach { storage.releasePlayerData(it) }
        storage.close()

        storage = createStorage()
        CustomCurrency.load()
        ShopManager.load()

        Bukkit.getOnlinePlayers().forEach { storage.loadPlayerData(it) }
    }

    private fun createStorage(): Storage {
        return if (config.getString("storage.type").equals("mysql", true)) {
            MysqlStorage(config.getConfigurationSection("storage.mysql")!!)
        } else {
            SQLiteStorage()
        }
    }

}
