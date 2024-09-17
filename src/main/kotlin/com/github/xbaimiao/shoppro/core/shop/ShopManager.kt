package com.github.xbaimiao.shoppro.core.shop

import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object ShopManager {

    val shops = ArrayList<Shop>()

    fun load() {
        val fileDirectory = File(plugin.dataFolder, "shops")

        var files = fileDirectory.listFiles()
        if (files == null || files.isEmpty()) {
            plugin.saveResource("shops${File.separator}limit-buy.yml", false)
            plugin.saveResource("shops${File.separator}limit-sell.yml", false)
            plugin.saveResource("shops${File.separator}sell.yml", false)
            files = fileDirectory.listFiles()
        }
        files?.forEach { file ->
            shops.add(ShopImpl(YamlConfiguration.loadConfiguration(file)))
        }
        info("加载了 ${shops.size} 个商店配置")
    }

}
