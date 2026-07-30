package com.xbaimiao.shoppro.currency

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.shoppro.util.format
import com.xbaimiao.shoppro.util.hasPlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

/**
 * 自定义货币
 *
 * 靠 currencys 目录下的配置文件描述: 用命令扣款/给款, 用 PAPI 变量读余额
 */
class CustomCurrency(configuration: Configuration) : Currency {

    val name: String = configuration.getString("name")!!
    override val alias: String = configuration.getString("display-name")
        ?.takeIf { it.isNotBlank() }
        ?: name
    private val takeCommand: String = configuration.getString("take-command")!!
    private val giveCommand: String = configuration.getString("give-command")!!
    private val balancePlaceholder: String = configuration.getString("balance-placeholder")!!

    init {
        if (!hasPlaceholderAPI) {
            error("自定义货币 $name 需要安装 PlaceholderAPI")
        }
    }

    override fun hasMoney(player: Player, amount: Double): Boolean {
        return getMoney(player) >= amount.format()
    }

    override fun giveMoney(player: Player, amount: Double) {
        dispatch(giveCommand, player, amount.format())
    }

    override fun takeMoney(player: Player, amount: Double): Boolean {
        if (!hasMoney(player, amount)) {
            return false
        }
        dispatch(takeCommand, player, amount.format())
        return true
    }

    override fun getMoney(player: Player): Double {
        val parsed = balancePlaceholder.replacePlaceholder(player)
        return parsed.toDoubleOrNull()?.format() ?: run {
            warn("自定义货币 $name 的余额变量解析结果 \"$parsed\" 不是数字, 按 0 处理")
            0.0
        }
    }

    private fun dispatch(command: String, player: Player, amount: Double) {
        val parsed = command
            .replace("%player%", player.name)
            .replace("%num%", amount.toString())
            .replacePlaceholder(player)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed)
    }

    companion object {

        private val cache = ArrayList<CustomCurrency>()

        fun load() {
            cache.clear()
            val directory = File(plugin.dataFolder, "currencys")
            var files = directory.listFiles()
            if (files == null || files.isEmpty()) {
                plugin.saveResource("currencys/example.yml", false)
                files = directory.listFiles() ?: emptyArray()
            }
            files.filter { it.isFile && it.extension.equals("yml", true) }.forEach { file ->
                runCatching {
                    cache += CustomCurrency(YamlConfiguration.loadConfiguration(file))
                }.onFailure {
                    warn("加载自定义货币 ${file.name} 失败: ${it.message}")
                }
            }
            info("加载了 ${cache.size} 个自定义货币")
        }

        fun byName(name: String): CustomCurrency {
            return cache.firstOrNull { it.name == name }
                ?: error("找不到名为 $name 的自定义货币, 请检查 currencys 目录")
        }

    }

}
