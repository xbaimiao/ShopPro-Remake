package com.github.xbaimiao.shoppro.core.vault

import org.bukkit.entity.Player
import java.util.*

interface Currency {

    fun hasMoney(player: Player, double: Double): Boolean

    fun giveMoney(player: Player, double: Double)

    fun takeMoney(player: Player, double: Double): Boolean

    fun getMoney(player: Player): Double

}

enum class CurrencyType(val string: String, val func: (name: String) -> Currency) {

    VAULT("vault", { VaultImpl }),
    POINTS("points", {
        PointsImpl()
    }),
    DIY(UUID.randomUUID().toString(), {
        DiyCurrency.formString(it)
    });

    companion object {
        fun formString(string: String): CurrencyType {
            return CurrencyType.values().firstOrNull { it.string == string.lowercase() } ?: DIY
        }

    }

}