package com.xbaimiao.shoppro.item

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.shoppro.ShopPro
import com.xbaimiao.shoppro.currency.Currency
import com.xbaimiao.shoppro.shop.Shop
import com.xbaimiao.shoppro.shop.ShopType
import com.xbaimiao.shoppro.util.countItems
import com.xbaimiao.shoppro.util.format
import com.xbaimiao.shoppro.util.modifyLore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 可交易的商品
 */
abstract class ShopItem(
    override val key: Char,
    override val material: Material,
    override val name: String,
    override val lore: List<String>,
    override val vanilla: Boolean,
    override val commands: List<String>,
    override val shop: Shop,
    override val enableRightClick: Boolean,
    override val conditionScript: String?,
    override val conditionIcon: Material?,
    override val conditionLore: List<String>,
    override val conditionName: String?,
    val price: Double,
    val limitServer: Long,
    val currency: Currency,
    val data: Int,
    private val limitPlayer: Long,
    private val limitPermissions: Map<String, Long>,
) : Item, ItemCondition {

    constructor(setting: ItemSetting) : this(
        key = setting.key,
        material = setting.material,
        name = setting.name,
        lore = setting.lore,
        vanilla = setting.vanilla,
        commands = setting.commands,
        shop = setting.shop,
        enableRightClick = setting.enableRightClick,
        conditionScript = setting.conditionScript,
        conditionIcon = setting.conditionIcon,
        conditionLore = setting.conditionLore,
        conditionName = setting.conditionName,
        price = setting.price,
        limitServer = setting.limitServer,
        currency = setting.currency,
        data = setting.data,
        limitPlayer = setting.limitPlayer,
        limitPermissions = setting.limitPermissions
    )

    /**
     * 从配置里读出来的商品参数
     */
    class ItemSetting(
        val key: Char,
        val material: Material,
        val name: String,
        val lore: List<String>,
        val vanilla: Boolean,
        val commands: List<String>,
        val shop: Shop,
        val enableRightClick: Boolean,
        val conditionScript: String?,
        val conditionIcon: Material?,
        val conditionLore: List<String>,
        val conditionName: String?,
        val price: Double,
        val limitServer: Long,
        val limitPlayer: Long,
        val limitPermissions: Map<String, Long>,
        val currency: Currency,
        val data: Int,
    )

    override fun isCommodity(): Boolean = true

    /**
     * 构建真正发给玩家的物品
     *
     * 和 [createIcon] 的区别是这个不带商店界面上的价格 lore
     */
    abstract fun buildVanillaItem(player: Player): ItemStack

    /**
     * 判断玩家背包里的物品是不是这个商品
     */
    abstract fun matches(itemStack: ItemStack): Boolean

    /**
     * 是否启用了限购
     *
     * 只配了其中一项也算启用, 未配置的那项按不限制处理
     */
    fun isLimited(): Boolean {
        return limitPlayer != 0L || limitServer != 0L
    }

    /**
     * 玩家的个人限额, 取其拥有的权限里限额最高的一档
     */
    fun getLimitPlayer(player: Player): Long {
        return limitPermissions
            .filter { player.hasPermission(it.key) }
            .maxOfOrNull { it.value }
            ?: limitPlayer
    }

    override fun update(player: Player): ItemStack {
        val isBuyShop = shop.getType() == ShopType.BUY
        val playerData = ShopPro.inst.storage.getPlayerData(player, this)
        val serverData = ShopPro.inst.storage.getServerData(this)
        val playerUsed = if (isBuyShop) playerData.buy else playerData.sell
        val serverUsed = if (isBuyShop) serverData.buy else serverData.sell

        val limitPlayerValue = getLimitPlayer(player)
        val balance = currency.getMoney(player)

        return createIcon(player).modifyLore {
            val replaced = map { line ->
                var result = line
                    .replaceVariable("name", name)
                    .replaceVariable("price", price.toString())
                    .replaceVariable("money", balance.toString())
                    .replaceVariable("price64", (price * 64).toString())
                    .replaceVariable("limit", limitPlayerValue.toString())
                    .replaceVariable("allLimit", limitServer.toString())
                    .replaceVariable("limit-player", (limitPlayerValue - playerUsed).toString())
                    .replaceVariable("limit-server", (limitServer - serverUsed).toString())
                if (!isBuyShop) {
                    val total = player.inventory.countItems { matches(it) } * price
                    result = result.replaceVariable("priceAll", total.format().toString())
                }
                result.replacePlaceholder(player)
            }
            clear()
            addAll(replaced)
        }
    }

    /**
     * 变量支持 {x} 和 ${x} 两种写法
     */
    private fun String.replaceVariable(variable: String, value: String): String {
        return this.replace("\${$variable}", value).replace("{$variable}", value)
    }

}
