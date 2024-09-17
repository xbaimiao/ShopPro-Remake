package com.github.xbaimiao.shoppro.core.shop

import com.github.xbaimiao.shoppro.ShopPro
import com.github.xbaimiao.shoppro.api.ShopProBuyEvent
import com.github.xbaimiao.shoppro.api.ShopProSellEvent
import com.github.xbaimiao.shoppro.core.database.LimitData
import com.github.xbaimiao.shoppro.core.item.Item
import com.github.xbaimiao.shoppro.core.item.KetherCondition
import com.github.xbaimiao.shoppro.core.item.ShopItem
import com.github.xbaimiao.shoppro.util.Util.howManyItems
import com.github.xbaimiao.shoppro.util.Util.replacePapi
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submitAsync
import taboolib.module.chat.colored
import taboolib.module.ui.ClickType
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.giveItem
import taboolib.platform.util.hasItem
import taboolib.platform.util.sendLang
import taboolib.platform.util.takeItem
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class ShopImpl(private val configuration: Configuration) : Shop() {

    private val slots = configuration.getStringList("slots").map { it.toCharArray().toList() }
    private val clickCache = WeakHashMap<Player, Long>()

    private fun canClick(player: Player): Boolean {
        val lastClick = clickCache[player]
        if (lastClick == null) {
            clickCache[player] = System.currentTimeMillis()
            return true
        }
        if (System.currentTimeMillis() - lastClick < 200) {
            return false
        }
        clickCache[player] = System.currentTimeMillis()
        return true
    }

    private val items = ArrayList<Item>()

    init {
        val section = configuration.getConfigurationSection("items")!!
        a@ for (key in section.getKeys(false)) {
            try {
                val subSection = section.getConfigurationSection(key)!!
                if (section.getBoolean("$key.is-commodity", true)) {
                    val materialString = section.getString("$key.material")!!
                    for (loader in ShopPro.itemLoaderManager.itemLoaders) {
                        if (loader.prefix != null) {
                            if (materialString.startsWith(loader.prefix!!)) {
                                items.add(loader.formSection(key[0], subSection, this))
                                continue@a
                            }
                        }
                    }
                    items.add(ShopPro.itemLoaderManager.getVanillaShop().formSection(key[0], subSection, this))
                } else {
                    items.add(ShopPro.itemLoaderManager.getItemImpl().formSection(key[0], subSection, this))
                }
            } catch (e: Throwable) {
                info("在加载Shop: ${getName()} 时,物品: $key 加载出现异常,跳过加载,错误信息如下")
                e.printStackTrace()
            }
        }
    }

    override fun getTitle(player: Player): String {
        return configuration.getString("title")!!.colored().replacePapi(player)
    }

    override fun getType(): ShopType {
        return ShopType.formString(configuration.getString("type")!!)
    }

    override fun getName(): String {
        return configuration.getString("name")!!.colored()
    }

    override fun sellAll(player: Player) {
        if (getType() != ShopType.SELL) {
            throw RuntimeException("此商店非出售商店")
        }
        items.filterIsInstance<ShopItem>().forEach { shopItem ->
            val amount = player.inventory.howManyItems {
                shopItem.equal(it)
            }
            if (amount > 0) {
                checkSellLimit(amount, shopItem, player)
            }
        }
    }

    override fun open(player: Player) {
        player.openMenu<Basic>(getTitle(player)) {
            rows(slots.size)
            slots = CopyOnWriteArrayList<List<Char>>().also { it.addAll(this@ShopImpl.slots) }
            onClick {
                it.isCancelled = true
            }
            for (item in this@ShopImpl.items) {
                var canBuyOrSell = true
                if (item is KetherCondition && !item.check(player)) {
                    canBuyOrSell = false
                }
                if (!canBuyOrSell && item is KetherCondition) {
                    set(item.key, item.conditionItem(player))
                    continue
                }
                set(item.key, item.update(player))
                // buy and sell
                if (item is ShopItem) {
                    if (getType() == ShopType.BUY) {
                        onClick(item.key) { event ->
                            if (!canClick(player)) {
                                return@onClick
                            }
                            if (event.clickType != ClickType.CLICK) {
                                return@onClick
                            }
                            val amount = when (event.clickEvent().click) {
                                org.bukkit.event.inventory.ClickType.LEFT -> 1
                                org.bukkit.event.inventory.ClickType.RIGHT -> if (item.enableRight) 64 else 1
                                else -> return@onClick
                            }
                            checkBuyLimit(amount, item, player)
                            event.currentItem?.let {
                                event.clickEvent().inventory.setItem(event.rawSlot, item.update(player))
                            }
                        }
                    }
                    if (getType() == ShopType.SELL) {
                        onClick(item.key) { event ->
                            if (!canClick(player)) {
                                return@onClick
                            }
                            if (event.clickType != ClickType.CLICK) {
                                return@onClick
                            }
                            val amount = when (event.clickEvent().click) {
                                org.bukkit.event.inventory.ClickType.LEFT -> 1
                                org.bukkit.event.inventory.ClickType.RIGHT -> if (item.enableRight) 64 else 1
                                org.bukkit.event.inventory.ClickType.SHIFT_RIGHT -> {
                                    player.inventory.howManyItems {
                                        item.equal(it)
                                    }
                                }

                                else -> return@onClick
                            }


                            checkSellLimit(amount, item, player)
                            event.currentItem?.let {
                                event.clickEvent().inventory.setItem(event.rawSlot, item.update(player))
                            }
                        }
                    }
                } else {
                    onClick(item.key) {
                        if (!canClick(player)) {
                            return@onClick
                        }
                        item.exeCommands(player, 1)
                    }
                }
            }
        }
    }

    override fun getItems(): Collection<Item> {
        return items.toList()
    }

    private fun checkBuyLimit(amount: Int, item: ShopItem, player: Player) {
        if (item.isLimit()) {
            if (ShopPro.database.getPlayerAlreadyData(player, item).buy >= item.getLimitPlayer(player)) {
                player.sendLang("buy-limit-player", item.getLimitPlayer(player))
                ShopPro.config.getString("the_voice_of_failure")?.let {
                    player.playSound(player.location, it, 100f, 1f)
                }
                return
            }
            if (ShopPro.database.getServerAlreadyData(item).buy >= item.limitServer) {
                player.sendLang("buy-limit-server", item.limitServer)
                ShopPro.config.getString("the_voice_of_failure")?.let {
                    player.playSound(player.location, it, 100f, 1f)
                }
                return
            }
            if (ShopPro.database.getPlayerAlreadyData(player, item).buy + amount > item.getLimitPlayer(player)) {
                buy(
                    (item.getLimitPlayer(player) - ShopPro.database.getPlayerAlreadyData(player, item).buy).toInt(),
                    item,
                    player
                )
                return
            }
            if (ShopPro.database.getServerAlreadyData(item).buy + amount > item.limitServer) {
                buy((item.limitServer - ShopPro.database.getServerAlreadyData(item).buy).toInt(), item, player)
                return
            }
        }
        buy(amount, item, player)
    }

    private fun buy(amount: Int, item: ShopItem, player: Player) {
        if (item.currency.takeMoney(player, item.price * amount)) {
            if (item.vanilla) {
                val vanilla = item.vanillaItem(player)
                vanilla.amount = amount
                player.giveItem(vanilla)
            }
            Bukkit.getPluginManager().callEvent(ShopProBuyEvent(item, amount, player))
            ShopPro.database.addAmount(item, player, LimitData(amount.toLong(), 0L))
            item.exeCommands(player, amount)
            player.sendLang("buy-item", amount, item.name, String.format("%.2f", item.price * amount))
            ShopPro.config.getString("the_voice_of_success")?.let {
                player.playSound(player.location, it, 100f, 1f)
            }
        } else {
            player.sendLang("not-money")
            ShopPro.config.getString("the_voice_of_failure")?.let {
                player.playSound(player.location, it, 100f, 1f)
            }
        }
    }

    override fun sellItem(player: Player, shopItem: ShopItem, itemStack: ItemStack): Boolean {
        val amount = itemStack.amount
        if (shopItem.isLimit()) {
            val alreadyPlayerData = ShopPro.database.getPlayerAlreadyData(player, shopItem)
            if (alreadyPlayerData.sell >= shopItem.getLimitPlayer(player)) {
                player.sendLang("sell-limit-player", shopItem.getLimitPlayer(player))
                ShopPro.config.getString("the_voice_of_failure")?.let {
                    player.playSound(player.location, it, 100f, 1f)
                }
                return false
            }
            val alreadyServerData = ShopPro.database.getServerAlreadyData(shopItem)
            if (alreadyServerData.sell >= shopItem.limitServer) {
                player.sendLang("sell-limit-server", shopItem.limitServer)
                ShopPro.config.getString("the_voice_of_failure")?.let {
                    player.playSound(player.location, it, 100f, 1f)
                }
                return false
            }
            if (alreadyPlayerData.sell + amount > shopItem.getLimitPlayer(player)) {
                sell((shopItem.getLimitPlayer(player) - alreadyPlayerData.sell).toInt(), shopItem, player)
                return false
            }
            if (alreadyServerData.sell + amount > shopItem.limitServer) {
                sell((shopItem.limitServer - alreadyServerData.sell).toInt(), shopItem, player)
                return false
            }
        }
        if (!shopItem.equal(itemStack)) {
            error("!item.equal(itemStack)")
        }

        shopItem.currency.giveMoney(player, shopItem.price * amount)
        submitAsync {
            if (shopItem.isLimit()) {
                ShopPro.database.addAmount(shopItem, player, LimitData(0L, amount.toLong()))
            }
        }
        Bukkit.getPluginManager().callEvent(ShopProSellEvent(shopItem, amount, player))
        player.sendLang("sell-item", amount, shopItem.name, String.format("%.2f", shopItem.price * amount))
        ShopPro.config.getString("the_voice_of_success")?.let {
            player.playSound(player.location, it, 100f, 1f)
        }
        return true
    }

    private fun checkSellLimit(amount: Int, item: ShopItem, player: Player) {
        if (item.isLimit()) {
            if (ShopPro.database.getPlayerAlreadyData(player, item).sell >= item.getLimitPlayer(player)) {
                player.sendLang("sell-limit-player", item.getLimitPlayer(player))
                ShopPro.config.getString("the_voice_of_failure")?.let {
                    player.playSound(player.location, it, 100f, 1f)
                }
                return
            }
            if (ShopPro.database.getServerAlreadyData(item).sell >= item.limitServer) {
                player.sendLang("sell-limit-server", item.limitServer)
                ShopPro.config.getString("the_voice_of_failure")?.let {
                    player.playSound(player.location, it, 100f, 1f)
                }
                return
            }
            if (ShopPro.database.getPlayerAlreadyData(player, item).sell + amount > item.getLimitPlayer(player)) {
                sell(
                    (item.getLimitPlayer(player) - ShopPro.database.getPlayerAlreadyData(player, item).sell).toInt(),
                    item,
                    player
                )
                return
            }
            if (ShopPro.database.getServerAlreadyData(item).sell + amount > item.limitServer) {
                sell((item.limitServer - ShopPro.database.getServerAlreadyData(item).sell).toInt(), item, player)
                return
            }
        }
        sell(amount, item, player)
    }

    private fun sell(amount: Int, item: ShopItem, player: Player) {
        if (player.inventory.hasItem(amount) { item.equal(it) }) {
            player.inventory.takeItem(amount) { item.equal(it) }
            item.currency.giveMoney(player, item.price * amount)
            Bukkit.getPluginManager().callEvent(ShopProSellEvent(item, amount, player))
            ShopPro.database.addAmount(item, player, LimitData(0L, amount.toLong()))
            player.sendLang("sell-item", amount, item.name, String.format("%.2f", item.price * amount))
            ShopPro.config.getString("the_voice_of_success")?.let {
                player.playSound(player.location, it, 100f, 1f)
            }
        } else {
            ShopPro.config.getString("the_voice_of_failure")?.let {
                player.playSound(player.location, it, 100f, 1f)
            }
            player.sendLang("not-item")
        }
    }

}
