package com.xbaimiao.shoppro.storage

/**
 * 某个商品的限购计数
 *
 * @param buy 已购买数量
 * @param sell 已出售数量
 */
data class LimitData(
    val buy: Long,
    val sell: Long,
) {

    operator fun plus(other: LimitData): LimitData {
        return LimitData(buy + other.buy, sell + other.sell)
    }

    /**
     * 序列化成数据库里存的 "买/卖" 格式
     */
    override fun toString(): String {
        return "$buy/$sell"
    }

    companion object {

        val EMPTY = LimitData(0L, 0L)

        fun parse(string: String): LimitData {
            val parts = string.split("/")
            if (parts.size != 2) {
                error("$string 不是合法的限购数据")
            }
            return LimitData(
                parts[0].toLongOrNull() ?: 0L,
                parts[1].toLongOrNull() ?: 0L
            )
        }

    }

}
