package com.xbaimiao.shoppro.currency

/**
 * 货币类型
 *
 * 配置里 currency 可以填:
 *   vault           走 Vault 经济
 *   points          走 PlayerPoints 点券
 *   fusang:货币ID    走 FusangLedger 的对应货币
 *   其它            按 currencys 目录下的自定义货币名查找
 */
enum class CurrencyType {

    VAULT,
    POINTS,
    FUSANG,
    CUSTOM;

    companion object {

        /**
         * 按配置里写的货币名解析出对应的 [Currency]
         */
        fun parse(name: String): Currency {
            return when {
                name.equals("vault", true) -> VaultCurrency
                name.equals("points", true) -> PointsCurrency
                name.startsWith("${FusangCurrency.PREFIX}:", true) -> FusangCurrency.parse(name)
                else -> CustomCurrency.byName(name)
            }
        }

    }

}
