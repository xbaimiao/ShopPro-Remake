package com.github.xbaimiao.shoppro.core.database

data class LimitData(
    val buy: Long,
    val sell: Long
) {

    companion object {

        fun ofNull(): LimitData = LimitData(0L, 0L)

        fun formString(string: String): LimitData {
            if (!string.contains("/")) {
                error("错误的String，非 LimitData")
            }
            return string.split("/").let {
                LimitData(it[0].toLong(), it[1].toLong())
            }
        }

    }

    fun add(limitData: LimitData): LimitData {
        return LimitData(limitData.buy + buy, limitData.sell + sell)
    }

    override fun toString(): String {
        return "$buy/$sell"
    }

}