package net.tjalp.aquarium.manager

import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.registry.*
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType.*
import java.util.*

/**
 * Manages the [Chunk] ownership, members, permissions, etc.
 */
class ChunkManager {

    private val loader = Aquarium.loader

    /**
     * Set the master of a chunk
     *
     * @param player The player to master the chunk
     * @param chunk The chunk to master
     */
    fun setMaster(player: Player, chunk: Chunk = player.chunk) {
        val chunkPdc = chunk.persistentDataContainer
        val playerPdc = player.persistentDataContainer
        val context = playerPdc.adapterContext
        val container = context.newPersistentDataContainer()
        val chunkDataContainer = playerPdc.get(PLAYER_CHUNKS, TAG_CONTAINER_ARRAY)?.toMutableSet() ?: mutableSetOf()

        container.set(X_COORDINATE, INTEGER, chunk.x)
        container.set(Z_COORDINATE, INTEGER, chunk.z)
        container.set(WORLD, STRING, chunk.world.uid.toString())
        chunkDataContainer.add(container)

        chunkPdc.set(CHUNK_MASTER, STRING, player.uniqueId.toString())
        //playerPdc.set(PLAYER_CHUNKS, ChunkArrayDataType, chunks.toTypedArray())
        playerPdc.set(PLAYER_CHUNKS, TAG_CONTAINER_ARRAY, chunkDataContainer.toTypedArray())
    }

    /**
     * Gets the master of a chunk
     *
     * @param chunk The chunk to get the master of
     * @return The master's [UUID], or null if none
     */
    fun getMaster(chunk: Chunk): UUID? {
        val pdc = chunk.persistentDataContainer

        return if (hasMaster(chunk)) UUID.fromString(pdc.get(CHUNK_MASTER, STRING)) else null
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
        val chunks = pdc.getOrDefault(PLAYER_CHUNKS, TAG_CONTAINER_ARRAY, arrayOf()).toSet()
        val chunkSet = mutableSetOf<Chunk>()

        for (chunk in chunks) {
            val worldUniqueId = UUID.fromString(chunk.get(WORLD, STRING)) ?: continue
            val world = loader.server.getWorld(worldUniqueId) ?: continue
            val x = chunk.get(X_COORDINATE, INTEGER) ?: continue
            val z = chunk.get(Z_COORDINATE, INTEGER) ?: continue

            chunkSet.add(world.getChunkAt(x, z))
        }

        return chunkSet

        //return pdc.getOrDefault(PLAYER_CHUNKS, ChunkArrayDataType, arrayOf()).toCollection(mutableSetOf())
    }
}