package com.xbaimiao.shoppro.shop

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.ui.SpigotBasic
import com.xbaimiao.easylib.util.giveItem
import com.xbaimiao.easylib.util.hasItem
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.takeItem
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.shoppro.ShopPro
import com.xbaimiao.shoppro.api.ShopProBuyEvent
import com.xbaimiao.shoppro.api.ShopProSellEvent
import com.xbaimiao.shoppro.item.Item
import com.xbaimiao.shoppro.item.ItemCondition
import com.xbaimiao.shoppro.item.ShopItem
import com.xbaimiao.shoppro.storage.LimitData
import com.xbaimiao.shoppro.util.countItems
import com.xbaimiao.shoppro.util.splitByStackSize
import org.bukkit.configuration.Configuration
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.util.WeakHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 由 yml 配置驱动的商店
 */
class ShopImpl(private val configuration: Configuration) : Shop() {

    private val layout = configuration.getStringList("layout").map { it.toCharArray().toList() }

    private val items = ArrayList<Item>()

    /** 上次点击时间, 防止连点导致的重复交易 */
    private val lastClick = WeakHashMap<Player, Long>()

    init {
        val section = configuration.getConfigurationSection("items")
            ?: error("商店 ${getName()} 缺少 items 配置节点")

        for (key in section.getKeys(false)) {
            runCatching {
                val itemSection = section.getConfigurationSection(key)
                    ?: error("items.$key 不是一个配置节点")

                items += if (itemSection.getBoolean("is-commodity", true)) {
                    val material = itemSection.getString("material")
                        ?: error("items.$key 缺少 material 配置")
                    val loader = ShopPro.inst.itemLoaderManager.matchLoader(material)
                        ?: ShopPro.inst.itemLoaderManager.vanillaLoader()
                    loader.fromSection(key[0], itemSection, this)
                } else {
                    ShopPro.inst.itemLoaderManager.decorationLoader().fromSection(key[0], itemSection, this)
                }
            }.onFailure {
                warn("加载商店 ${getName()} 的物品 $key 时出错, 已跳过: ${it.message}")
                it.printStackTrace()
            }
        }
        info("商店 ${getName()} 加载了 ${items.size} 个物品")
    }

    override fun getTitle(player: Player): String {
        return configuration.getString("title")!!.colored().replacePlaceholder(player)
    }

    override fun getType(): ShopType {
        return ShopType.parse(configuration.getString("type")!!)
    }

    override fun getName(): String {
        return configuration.getString("name")!!.colored()
    }

    override fun getItems(): Collection<Item> {
        return items.toList()
    }

    override fun open(player: Player) {
        val menu = SpigotBasic(player, getTitle(player))
        menu.apply {
            rows(layout.size)
            slots = CopyOnWriteArrayList(this@ShopImpl.layout)
            onClick { it.isCancelled = true }
            onDrag { it.isCancelled = true }

            for (item in this@ShopImpl.items) {
                // 条件不满足的商品只显示替代图标, 不绑定任何点击行为
                if (item is ItemCondition && !item.checkCondition(player)) {
                    set(item.key, item.buildConditionItem(player))
                    continue
                }
                set(item.key, item.update(player))

                if (item is ShopItem) {
                    bindTrade(this, item, player)
                } else {
                    onClick(item.key) {
                        if (canClick(player)) {
                            item.executeCommands(player, 1)
                        }
                    }
                }
            }
        }
        menu.open()
    }

    /**
     * 给商品绑定买卖点击
     */
    private fun bindTrade(menu: SpigotBasic, item: ShopItem, player: Player) {
        val shopType = getType()
        menu.onClick(item.key) { event ->
            if (!canClick(player)) {
                return@onClick
            }
            val amount = resolveAmount(event.click, item, player, shopType) ?: return@onClick

            when (shopType) {
                ShopType.BUY -> tryBuy(amount, item, player)
                ShopType.SELL -> trySell(amount, item, player)
            }

            // 交易后刷新图标上的余额和限购数字
            event.inventory.setItem(event.rawSlot, item.update(player))
        }
    }

    /**
     * 按点击类型算出这次交易的数量
     *
     * @return null 表示该点击类型不触发交易
     */
    private fun resolveAmount(click: ClickType, item: ShopItem, player: Player, shopType: ShopType): Int? {
        return when (click) {
            ClickType.LEFT -> 1
            ClickType.RIGHT -> if (item.enableRightClick) 64 else 1
            // 一键出售背包内全部, 只有收购商店才有意义
            ClickType.SHIFT_RIGHT -> {
                if (shopType != ShopType.SELL) return null
                player.inventory.countItems { item.matches(it) }
            }

            else -> null
        }
    }

    /**
     * 50ms 内的重复点击直接丢弃
     */
    private fun canClick(player: Player): Boolean {
        val now = System.currentTimeMillis()
        val last = lastClick[player]
        if (last != null && now - last < CLICK_INTERVAL) {
            return false
        }
        lastClick[player] = now
        return true
    }

    override fun sellAll(player: Player) {
        if (getType() != ShopType.SELL) {
            error("商店 ${getName()} 不是收购商店")
        }
        items.filterIsInstance<ShopItem>().forEach { item ->
            // 条件不满足的商品不参与一键出售
            if (!item.checkCondition(player)) {
                return@forEach
            }
            val amount = player.inventory.countItems { item.matches(it) }
            if (amount > 0) {
                trySell(amount, item, player)
            }
        }
    }

    /**
     * 按限购把交易数量压到允许的上限, 再执行购买
     */
    private fun tryBuy(amount: Int, item: ShopItem, player: Player) {
        val allowed = clampByLimit(amount, item, player, isBuy = true) ?: return
        buy(allowed, item, player)
    }

    private fun trySell(amount: Int, item: ShopItem, player: Player) {
        val allowed = clampByLimit(amount, item, player, isBuy = false) ?: return
        sell(allowed, item, player)
    }

    /**
     * 计算限购允许的实际交易数量
     *
     * @return null 表示已达上限不能交易, 消息已经发给玩家了
     */
    private fun clampByLimit(amount: Int, item: ShopItem, player: Player, isBuy: Boolean): Int? {
        if (!item.isLimited()) {
            return amount
        }
        val playerData = ShopPro.inst.storage.getPlayerData(player, item)
        val serverData = ShopPro.inst.storage.getServerData(item)
        val playerUsed = if (isBuy) playerData.buy else playerData.sell
        val serverUsed = if (isBuy) serverData.buy else serverData.sell
        val playerLimit = item.getLimitPlayer(player)

        if (playerUsed >= playerLimit) {
            fail(player, if (isBuy) "buy-limit-player" else "sell-limit-player", playerLimit)
            return null
        }
        if (serverUsed >= item.limitServer) {
            fail(player, if (isBuy) "buy-limit-server" else "sell-limit-server", item.limitServer)
            return null
        }
        // 超出部分截掉, 按剩余额度成交
        val playerRemaining = playerLimit - playerUsed
        val serverRemaining = item.limitServer - serverUsed
        return minOf(amount.toLong(), playerRemaining, serverRemaining).toInt()
    }

    private fun buy(amount: Int, item: ShopItem, player: Player) {
        if (amount <= 0) {
            return
        }
        val cost = item.price * amount
        if (!item.currency.takeMoney(player, cost)) {
            fail(player, "not-money")
            return
        }
        if (item.vanilla) {
            // 超过最大堆叠数时要拆成多堆, 否则会丢东西
            item.buildVanillaItem(player).splitByStackSize(amount).forEach { player.giveItem(it) }
        }
        ShopProBuyEvent(item, amount, player).call()
        ShopPro.inst.storage.addAmount(item, player, LimitData(amount.toLong(), 0L))
        item.executeCommands(player, amount)
        player.sendLang("buy-item", amount, item.name, item.currency.displayAmount(cost))
        playSound(player, "success")
    }

    private fun sell(amount: Int, item: ShopItem, player: Player) {
        // 数量为 0 说明玩家背包里没有可卖的东西
        if (amount <= 0 || !player.inventory.hasItem(amount) { item.matches(this) }) {
            fail(player, "not-item")
            return
        }
        // 先扣物品再给钱, 扣不掉就不给钱
        player.inventory.takeItem(amount) { item.matches(this) }
        val income = item.price * amount
        item.currency.giveMoney(player, income)
        ShopProSellEvent(item, amount, player).call()
        ShopPro.inst.storage.addAmount(item, player, LimitData(0L, amount.toLong()))
        player.sendLang("sell-item", amount, item.name, item.currency.displayAmount(income))
        playSound(player, "success")
    }

    private fun fail(player: Player, langKey: String, vararg args: Any) {
        player.sendLang(langKey, *args)
        playSound(player, "failure")
    }

    private fun playSound(player: Player, type: String) {
        val sound = ShopPro.inst.config.getString("sound.$type")
        if (sound.isNullOrBlank()) {
            return
        }
        runCatching {
            player.playSound(player.location, sound, 1f, 1f)
        }.onFailure {
            warn("播放声音 $sound 失败: ${it.message}")
        }
    }

    companion object {
        /** 两次点击的最小间隔, 毫秒 */
        private const val CLICK_INTERVAL = 50L
    }

}
