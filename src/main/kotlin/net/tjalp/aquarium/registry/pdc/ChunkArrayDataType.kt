package net.tjalp.aquarium.registry.pdc

import net.tjalp.aquarium.Aquarium
import org.bukkit.Chunk
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.UUID

object ChunkArrayDataType : PersistentDataType<ByteArray, Array<Chunk>> {

    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java
    override fun getComplexType(): Class<Array<Chunk>> = Array<Chunk>::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Array<Chunk> {
        val buffer = ByteBuffer.wrap(primitive)
        val mutableSet = mutableSetOf<Chunk>()

        while (buffer.remaining() > 0) {
            val uniqueId = UUID(buffer.long, buffer.long)
            val world = Aquarium.loader.server.getWorld(uniqueId)
            val key = buffer.long

            if (world == null) {
                Aquarium.loader.logger.warning("World $uniqueId does not exist even though it is in a persistent data container!")
                continue
            }

            mutableSet.add(world.getChunkAt(key))
        }

        return mutableSet.toTypedArray()
    }

    override fun toPrimitive(complex: Array<Chunk>, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.wrap(ByteArray(complex.size * 24))
        val set = complex.toCollection(mutableSetOf())

        for (chunk in set) {
            val worldUniqueId = chunk.world.uid

            buffer
                .putLong(worldUniqueId.mostSignificantBits)
                .putLong(worldUniqueId.leastSignificantBits)
                .putLong(chunk.chunkKey)
        }

        return buffer.array()
    }
}