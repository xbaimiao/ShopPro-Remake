package com.github.xbaimiao.shoppro.core.shop

import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.platform.function.info
import taboolib.platform.BukkitPlugin
import java.io.File

object ShopManager {

    val shops = ArrayList<Shop>()

    fun load() {
        val fileDirectory = File(BukkitPlugin.getInstance().dataFolder, "shops")

        var files = fileDirectory.listFiles()
        if (files == null || files.isEmpty()) {
            BukkitPlugin.getInstance().saveResource("shops${File.separator}limit-buy.yml", false)
            BukkitPlugin.getInstance().saveResource("shops${File.separator}limit-sell.yml", false)
            BukkitPlugin.getInstance().saveResource("shops${File.separator}sell.yml", false)
            files = fileDirectory.listFiles()
        }
        files?.forEach { file ->
            shops.add(ShopImpl(YamlConfiguration.loadConfiguration(file)))
        }
        info("加载了 ${shops.size} 个商店配置")
    }

}