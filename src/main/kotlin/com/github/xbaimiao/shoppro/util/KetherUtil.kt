package com.github.xbaimiao.shoppro.util

import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.KetherShell
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * @author 小白
 * @date 2023/5/10 09:47
 **/
object KetherUtil {

    @JvmStatic
    fun eval(player: Player, script: String): CompletableFuture<Any?> {
        return try {
            KetherShell.eval(script, namespace = listOf("shoppro")) {
                sender = adaptPlayer(player)
            }
        } catch (e: LocalizedException) {
            println("§c[ShopPro] §8Unexpected exception while parsing kether shell:")
            e.localizedMessage.split("\n").forEach {
                println("         §8$it")
            }
            CompletableFuture.completedFuture(false)
        }
    }

    @JvmStatic
    fun instantKether(player: Player, script: String, timeout: Long = 100): EvalResult {
        return try {
            EvalResult(eval(player, script).get(timeout, TimeUnit.MILLISECONDS))
        } catch (e: TimeoutException) {
            println("§c[ShopPro] §8Timeout while parsing kether shell:")
            e.localizedMessage?.split("\n")?.forEach { println("         §8$it") }
            EvalResult.FALSE
        }
    }

}