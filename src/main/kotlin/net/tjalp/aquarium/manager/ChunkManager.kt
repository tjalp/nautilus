package net.tjalp.aquarium.manager

import net.tjalp.aquarium.registry.CHUNK_MASTER
import net.tjalp.aquarium.registry.PLAYER_CHUNKS
import net.tjalp.aquarium.registry.pdc.ChunkArrayDataType
import net.tjalp.aquarium.registry.pdc.UuidDataType
import org.bukkit.Chunk
import org.bukkit.entity.Player
import java.util.*

/**
 * Manages the [Chunk] ownership, members, permissions, etc.
 */
class ChunkManager {

    /**
     * Set the master of a chunk
     *
     * @param player The player to master the chunk
     * @param chunk The chunk to master
     */
    fun setMaster(player: Player, chunk: Chunk = player.chunk) {
        val chunkPdc = chunk.persistentDataContainer
        val playerPdc = player.persistentDataContainer
        val chunks = getMasteredChunks(player).toMutableSet()

        chunks.add(chunk)

        chunkPdc.set(CHUNK_MASTER, UuidDataType, player.uniqueId)
        playerPdc.set(PLAYER_CHUNKS, ChunkArrayDataType, chunks.toTypedArray())
    }

    /**
     * Gets the master of a chunk
     *
     * @param chunk The chunk to get the master of
     * @return The master's [UUID], or null if none
     */
    fun getMaster(chunk: Chunk): UUID? {
        val pdc = chunk.persistentDataContainer

        return if (hasMaster(chunk)) pdc.get(CHUNK_MASTER, UuidDataType) else null
    }

    /**
     * Whether a chunk has a master or not
     *
     * @param chunk The chunk to check
     * @return True when there's a master, otherwise false
     */
    fun hasMaster(chunk: Chunk): Boolean {
        val pdc = chunk.persistentDataContainer

        return pdc.has(CHUNK_MASTER)
    }

    /**
     * Gets a [Set] of mastered chunks of a player
     *
     * @param player The player to get the mastered chunks of
     * @return A [Set] of mastered chunks of the player
     */
    fun getMasteredChunks(player: Player): Set<Chunk> {
        val pdc = player.persistentDataContainer
        return pdc.getOrDefault(PLAYER_CHUNKS, ChunkArrayDataType, arrayOf()).toCollection(mutableSetOf())
    }
}