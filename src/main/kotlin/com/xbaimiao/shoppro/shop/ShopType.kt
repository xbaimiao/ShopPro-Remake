package com.xbaimiao.shoppro.shop

/**
 * 商店类型
 */
enum class ShopType(val id: String) {

    /** 玩家花钱买东西 */
    BUY("buy"),

    /** 玩家卖东西换钱 */
    SELL("sell");

    companion object {

        fun parse(id: String): ShopType {
            return entries.firstOrNull { it.id.equals(id, true) }
                ?: error("$id 不是合法的商店类型, 只能是 buy 或 sell")
        }

    }

}
