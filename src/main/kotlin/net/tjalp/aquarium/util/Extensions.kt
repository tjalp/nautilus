package net.tjalp.aquarium.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.minecraft.network.protocol.Packet
import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.command.DisguiseCommand
import net.tjalp.aquarium.item.CustomItem
import net.tjalp.aquarium.registry.CUSTOM_ITEM
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

fun Player.sendPacket(packet: Packet<*>) = (this as CraftPlayer).handle.networkManager.send(packet)
fun Listener.register() = Aquarium.loader.server.pluginManager.registerEvents(this, Aquarium.loader)

fun Player.getPrefix(): String? {
    val lp = Aquarium.luckperms

    val prefix: String? = if (DisguiseCommand.disguises.containsKey(this.uniqueId)) {
        val rank = DisguiseCommand.disguises[this.uniqueId]!!
        lp.groupManager.getGroup(rank)?.cachedData?.metaData?.prefix
    } else {
        val user = if (isOnline) lp.getPlayerAdapter(Player::class.java).getUser(this) else lp.userManager.loadUser(uniqueId).join()
        user.cachedData.metaData.prefix
    }

    return prefix
}

fun Player.getSuffix(): String? {
    val lp = Aquarium.luckperms

    val suffix: String? = if (DisguiseCommand.disguises.containsKey(this.uniqueId)) {
        val rank = DisguiseCommand.disguises[this.uniqueId]!!
        lp.groupManager.getGroup(rank)?.cachedData?.metaData?.suffix
    } else {
        val user = if (isOnline) lp.getPlayerAdapter(Player::class.java).getUser(this) else lp.userManager.loadUser(uniqueId).join()
        user.cachedData.metaData.suffix
    }

    return suffix
}

fun Player.getNameColor(): String? {
    val lp = Aquarium.luckperms

    val color: String? = if (DisguiseCommand.disguises.containsKey(this.uniqueId)) {
        val rank = DisguiseCommand.disguises[this.uniqueId]!!
        lp.groupManager.getGroup(rank)?.cachedData?.metaData?.getMetaValue("name_color")
    } else {
        val user = if (isOnline) lp.getPlayerAdapter(Player::class.java).getUser(this) else lp.userManager.loadUser(uniqueId).join()
        user.cachedData.metaData.getMetaValue("name_color")
    }

    return color
}

fun Player.getFormattedName(usePrefix: Boolean = true, useNameColor: Boolean = true, useSuffix: Boolean = true): Component {
    val mini = miniMessage()
    val prefix = mini.deserialize(getPrefix() ?: "")
    val suffix = mini.deserialize(getSuffix() ?: "")
    val username = mini.deserialize((getNameColor() ?: "") + name)
    val name = Component.text()

    if (usePrefix) name.append(prefix)
    if (useNameColor) name.append(username) else name.append(this.name())
    if (useSuffix) name.append(suffix)

    return name.build()
}

fun Player.getChatColor(): String {
    val lp = Aquarium.luckperms

    val color: String? = if (DisguiseCommand.disguises.containsKey(this.uniqueId)) {
        val rank = DisguiseCommand.disguises[this.uniqueId]!!
        lp.groupManager.getGroup(rank)?.cachedData?.metaData?.getMetaValue("chat_color")
    } else {
        val user = if (isOnline) lp.getPlayerAdapter(Player::class.java).getUser(this) else lp.userManager.loadUser(uniqueId).join()
        user.cachedData.metaData.getMetaValue("chat_color")
    }

    return color ?: "<reset>"
}

/**
 * See [net.tjalp.aquarium.manager.ChunkManager.getMasteredChunks]
 */
fun Player.getMasteredChunks(): Set<Chunk> {
    return Aquarium.chunkManager.getMasteredChunks(this)
}

/**
 * See [net.tjalp.aquarium.manager.ChunkManager.hasMaster]
 */
fun Chunk.hasMaster(): Boolean {
    return Aquarium.chunkManager.hasMaster(this)
}

/**
 * See [net.tjalp.aquarium.manager.ChunkManager.setMaster]
 */
fun Chunk.setMaster(player: Player) {
    Aquarium.chunkManager.setMaster(player, this)
}

/**
 * See [net.tjalp.aquarium.manager.ChunkManager.getMaster]
 */
fun Chunk.getMaster(): UUID? {
    return Aquarium.chunkManager.getMaster(this)
}

/**
 * Get the custom item from a normal ItemStack
 *
 * @return custom item, or null if non-existent
 */
fun ItemStack.getCustomItem(): CustomItem? {
    val meta = itemMeta ?: return null
    val pdc = meta.persistentDataContainer

    if (!pdc.has(CUSTOM_ITEM)) return null

    return Aquarium.itemRegistry.getItem(pdc.get(CUSTOM_ITEM, PersistentDataType.STRING)!!)
}