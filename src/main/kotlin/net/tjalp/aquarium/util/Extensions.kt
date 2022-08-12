package net.tjalp.aquarium.util

import net.minecraft.network.protocol.Packet
import net.tjalp.aquarium.Aquarium
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.UUID

fun Player.sendPacket(packet: Packet<*>) = (this as CraftPlayer).handle.networkManager.send(packet)
fun Listener.register() = Aquarium.loader.server.pluginManager.registerEvents(this, Aquarium.loader)

fun Player.getPrefix(): String? {
    val lp = Aquarium.luckperms
    val user = if (isOnline) lp.getPlayerAdapter(org.bukkit.entity.Player::class.java).getUser(this) else lp.userManager.loadUser(uniqueId).join()

    return user.cachedData.metaData.prefix
}

fun Player.getSuffix(): String? {
    val lp = Aquarium.luckperms
    val user = if (isOnline) lp.getPlayerAdapter(org.bukkit.entity.Player::class.java).getUser(this) else lp.userManager.loadUser(uniqueId).join()

    return user.cachedData.metaData.suffix
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