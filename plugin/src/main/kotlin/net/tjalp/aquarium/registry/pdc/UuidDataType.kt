package net.tjalp.aquarium.registry.pdc

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.*

object UuidDataType : PersistentDataType<ByteArray, UUID> {

    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java
    override fun getComplexType(): Class<UUID> = UUID::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): UUID {
        val buffer = ByteBuffer.wrap(primitive)
        val firstLong = buffer.long
        val secondLong = buffer.long
        return UUID(firstLong, secondLong)
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): ByteArray {
        val buffer: ByteBuffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(complex.mostSignificantBits)
        buffer.putLong(complex.leastSignificantBits)
        return buffer.array()
    }
}