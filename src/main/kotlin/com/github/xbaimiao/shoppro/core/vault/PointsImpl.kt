package com.github.xbaimiao.shoppro.core.vault

import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.PlayerPointsAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PointsImpl : Currency {

    private val pointsAPI: PlayerPointsAPI

    init {
        val plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints")?.let {
            it as PlayerPoints
        } ?: error("未找到PlayerPoints")
        pointsAPI = plugin.api
    }

    override fun hasMoney(player: Player, double: Double): Boolean {
        return getMoney(player) >= double
    }

    override fun giveMoney(player: Player, double: Double) {
        pointsAPI.give(player.uniqueId, double.toInt())
    }

    override fun takeMoney(player: Player, double: Double): Boolean {
        if (hasMoney(player, double)) {
            pointsAPI.take(player.uniqueId, double.toInt())
            return true
        }
        return false
    }

    override fun getMoney(player: Player): Double {
        return pointsAPI.look(player.uniqueId).toDouble()
    }

}