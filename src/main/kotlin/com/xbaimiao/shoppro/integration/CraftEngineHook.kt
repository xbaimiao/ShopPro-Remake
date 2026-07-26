package com.xbaimiao.shoppro.integration

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor
import net.momirealms.craftengine.bukkit.item.BukkitItem
import net.momirealms.craftengine.bukkit.item.BukkitItemManager
import net.momirealms.craftengine.core.item.BuildableItem
import net.momirealms.craftengine.core.item.ItemBuildContext
import net.momirealms.craftengine.core.util.Key
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * CraftEngine 软集成
 *
 * CraftEngine 是 softDepend, 未安装时本对象的所有方法安全降级返回 null
 * 只有在 [isLoaded] 为 true 时才会真正触碰 CraftEngine 的类
 * 避免未安装时抛 NoClassDefFoundError
 */
object CraftEngineHook {

    /** CraftEngine 是否已安装并启用 */
    val isLoaded: Boolean by lazy {
        Bukkit.getPluginManager().getPlugin("CraftEngine")?.isEnabled == true
    }

    /**
     * 取物品的 CraftEngine id, 形如 命名空间:路径
     *
     * 原版物品返回 minecraft:xxx, CE 未安装或异常返回 null
     */
    fun itemId(itemStack: ItemStack): String? {
        if (!isLoaded) return null
        return runCatching {
            BukkitItemManager.instance().wrap(itemStack).id().asString()
        }.getOrNull()
    }

    /**
     * 该 id 在 CraftEngine 里是否存在
     */
    fun exists(id: String): Boolean {
        if (!isLoaded) return false
        return runCatching { buildableOf(id) != null }.getOrDefault(false)
    }

    /**
     * 构建服务端侧真实物品, 用于发放给玩家
     *
     * 不做 s2c: CE 的动态 lore 是发包时才套上去的
     * 对真实物品做 s2c 会把客户端 lore 固化进服务端物品
     * 之后正常发包再套一次, 造成 lore 整段重复
     */
    fun buildItem(id: String, amount: Int, player: Player?): ItemStack? {
        if (!isLoaded) return null
        return runCatching {
            val buildable = buildableOf(id) ?: return null
            val context = ItemBuildContext.of(player?.let { BukkitAdaptor.adapt(it) })
            val built = buildable.buildItem(context, amount.coerceAtLeast(1)) as? BukkitItem ?: return null
            built.bukkitItem
        }.getOrNull()
    }

    /**
     * 构建面向 [viewer] 的客户端侧物品, 仅用于商店界面图标
     *
     * 走 s2c 才能拿到 CE 配置的真实名称和动态 lore
     * 这个结果只能进界面, 不能作为真实物品发给玩家
     */
    fun buildDisplayItem(id: String, viewer: Player): ItemStack? {
        if (!isLoaded) return null
        return runCatching {
            val buildable = buildableOf(id) ?: return null
            val cePlayer = BukkitAdaptor.adapt(viewer)
            val server = buildable.buildItem(ItemBuildContext.of(cePlayer), 1)
            val client = BukkitItemManager.instance().s2c(server, cePlayer)
            val result = if (client.isPresent) client.get() else server
            (result as? BukkitItem)?.bukkitItem
        }.getOrNull()
    }

    /**
     * 取 id 对应的可构建物品, 自定义物品与原版物品都能取到
     *
     * getBuildableItem 返回 Optional<? extends BuildableItem>
     * Kotlin 下对这种通配符 Optional 调 orElse(null) 会有型变问题, 所以用 isPresent 判断
     */
    private fun buildableOf(id: String): BuildableItem? {
        val optional = BukkitItemManager.instance().getBuildableItem(Key.of(id))
        return if (optional.isPresent) optional.get() else null
    }

}
