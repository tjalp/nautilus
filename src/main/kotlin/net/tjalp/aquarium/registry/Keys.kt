package net.tjalp.aquarium.registry

import net.tjalp.aquarium.Aquarium
import org.bukkit.NamespacedKey

val CHUNK_MASTER = key("chunk_master")
val CHUNK_MEMBERS = key("chunk_members")

val PLAYER_CHUNKS = key("chunks")
val X_COORDINATE = key("x")
val Z_COORDINATE = key("z")
val WORLD = key("world")

val CUSTOM_ITEM = key("custom_item")
val ICICLE = key("icicle")

private fun key(key: String): NamespacedKey = NamespacedKey(Aquarium.loader, key)