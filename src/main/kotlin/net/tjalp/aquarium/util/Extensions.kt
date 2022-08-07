package net.tjalp.aquarium.util

import net.minecraft.network.protocol.Packet
import net.tjalp.aquarium.Aquarium
import org.bukkit.World
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.event.Listener

fun org.bukkit.entity.Player.asNms() : net.minecraft.server.level.ServerPlayer {
    return (this as CraftPlayer).handle
}

fun net.minecraft.core.BlockPos.asBukkit(world: World) : org.bukkit.Location {
    return org.bukkit.Location(world, x.toDouble(), y.toDouble(), z.toDouble())
}

fun org.bukkit.Location.asNms() : net.minecraft.core.BlockPos {
    return net.minecraft.core.BlockPos(x, y, z)
}

fun org.bukkit.entity.Player.sendPacket(packet: Packet<*>) {
    this.asNms().networkManager.send(packet)
}

fun Listener.register() {
    Aquarium.loader.server.pluginManager.registerEvents(this, Aquarium.loader)
}

fun org.bukkit.entity.Player.getPrefix(): String? {
    val lp = Aquarium.luckperms
    val user = if (isOnline) lp.getPlayerAdapter(org.bukkit.entity.Player::class.java).getUser(this) else lp.userManager.loadUser(uniqueId).join()

    return user.cachedData.metaData.prefix
}

fun org.bukkit.entity.Player.getSuffix(): String? {
    val lp = Aquarium.luckperms
    val user = if (isOnline) lp.getPlayerAdapter(org.bukkit.entity.Player::class.java).getUser(this) else lp.userManager.loadUser(uniqueId).join()

    return user.cachedData.metaData.suffix
}