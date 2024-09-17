package com.github.xbaimiao.shoppro.core.vault

import com.github.xbaimiao.shoppro.util.Util
import com.github.xbaimiao.shoppro.util.Util.format
import com.github.xbaimiao.shoppro.util.Util.replacePapi
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.platform.BukkitPlugin
import taboolib.platform.compat.replacePlaceholder
import java.io.File

class DiyCurrency(configuration: Configuration) : Currency {

    companion object {
        private val cache = ArrayList<DiyCurrency>()

        fun load() {
            cache.clear()
            val file = File(BukkitPlugin.getInstance().dataFolder, "currencys")
            var listFile = file.listFiles()
            if (listFile == null || listFile.isEmpty()) {
                BukkitPlugin.getInstance().saveResource("currencys/Diy1.yml", false)
                listFile = file.listFiles()!!
            }
            listFile.forEach {
                cache.add(DiyCurrency(YamlConfiguration.loadConfiguration(it)))
            }
            info("加载了 ${cache.size} 个自定义货币")
        }

        fun formString(name: String): DiyCurrency {
            return cache.first { it.name == name }
        }

    }

    val name = configuration.getString("name")!!
    private val takeCommand = configuration.getString("takeCommand")!!
    private val giveCommand = configuration.getString("giveCommand")!!
    private val amountPapi = configuration.getString("amountPapi")!!

    init {
        if (!Util.hasPapi) {
            error("自定义货币必须安装PlaceholderAPI插件")
        }
    }

    override fun hasMoney(player: Player, double: Double): Boolean {
        return getMoney(player) >= double
    }

    override fun giveMoney(player: Player, double: Double) {
        val cmd = replace(giveCommand, player, double.format())
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
    }

    override fun takeMoney(player: Player, double: Double): Boolean {
        if (!hasMoney(player, double.format())) {
            return false
        }
        val cmd = replace(takeCommand, player, double.format())
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
        return true
    }

    override fun getMoney(player: Player): Double {
        return amountPapi.replacePapi(player).toDouble().format()
    }

    private fun replace(string: String, player: Player, num: Double): String {
        return string.replace("%player%", player.name).replace("%num%", num.toString()).replacePlaceholder(player)
    }

}
