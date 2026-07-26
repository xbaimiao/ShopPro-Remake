package com.xbaimiao.shoppro.currency

/**
 * 货币类型
 *
 * 配置里 currency 填 vault 或 points 走内置实现
 * 填其它值则按 currencys 目录下的自定义货币名查找
 */
enum class CurrencyType {

    VAULT,
    POINTS,
    CUSTOM;

    companion object {

        /**
         * 按配置里写的货币名解析出对应的 [Currency]
         */
        fun parse(name: String): Currency {
            return when (name.lowercase()) {
                "vault" -> VaultCurrency
                "points" -> PointsCurrency
                else -> CustomCurrency.byName(name)
            }
        }

    }

}
