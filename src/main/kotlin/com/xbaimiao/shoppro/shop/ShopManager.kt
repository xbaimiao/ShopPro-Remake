package com.xbaimiao.shoppro.shop

import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.warn
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * 管理所有商店配置
 */
object ShopManager {

    private val shops = ArrayList<Shop>()

    /** 首次启动时释放的示例配置 */
    private val defaultShops = listOf("buy.yml", "sell.yml", "sell-simple.yml")

    fun load() {
        shops.clear()
        val directory = File(plugin.dataFolder, "shops")

        var files = directory.listFiles()
        if (files == null || files.isEmpty()) {
            defaultShops.forEach { plugin.saveResource("shops/$it", false) }
            files = directory.listFiles() ?: emptyArray()
        }

        files.filter { it.isFile && it.extension.equals("yml", true) }.forEach { file ->
            runCatching {
                shops += ShopImpl(YamlConfiguration.loadConfiguration(file))
            }.onFailure {
                warn("加载商店配置 ${file.name} 失败: ${it.message}")
                it.printStackTrace()
            }
        }
        info("加载了 ${shops.size} 个商店配置")
    }

    fun all(): List<Shop> = shops.toList()

    fun byName(name: String): Shop? = shops.firstOrNull { it.getName() == name }

    fun names(): List<String> = shops.map { it.getName() }

}
