package net.tjalp.aquarium.util.dimension


import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.annotation.Nonnull
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Construct a new cuboid that exists of 2 positions
 *
 * @author John
 */
class Cuboid(
    var min: Position,
    var max: Position
) : Iterable<Position> {

    /**
     * Returns the center position of this Cuboid
     *
     * @return Position
     */
    val center: Position
        get() = Position(
            (min.x + max.x) / 2,
            (min.y + max.y) / 2,
            (min.z + max.z) / 2
        )

    /**
     * Return a grown version of this cuboid
     *
     * @param x X delta
     * @param y Y delta
     * @param z Z delta
     * @return New Cuboid
     */
    fun grow(x: Double, y: Double, z: Double): Cuboid {
        return Cuboid(
            Position(min.x - x, min.y - y, min.z - z),
            Position(max.x + x, max.y + y, max.z + z)
        )
    }

    /**
     * Returns a grown version of this cuboid
     *
     * @param delta Delta
     * @return New Cuboid
     */
    fun grow(delta: Double): Cuboid {
        return grow(delta, delta, delta)
    }

    /**
     * Return a shrunken version of this cuboid
     *
     * @param x X delta
     * @param y Y delta
     * @param z Z delta
     * @return New Cuboid
     */
    fun shrink(x: Double, y: Double, z: Double): Cuboid {
        return grow(-x, -y, -z)
    }

    /**
     * Returns a shrunken version of this cuboid
     *
     * @param delta Delta
     * @return New Cuboid
     */
    fun shrink(delta: Double): Cuboid {
        return shrink(delta, delta, delta)
    }

    /**
     * Perform an intersection check on
     * this cuboid with the given Cuboid.
     *
     * @param box The other cuboid
     * @return True if intersecting
     */
    fun intersects(box: Cuboid): Boolean {
        return intersects(box.min.x, box.min.y, box.min.z, box.max.x, box.max.y, box.max.z)
    }

    fun intersects(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): Boolean {
        return min.x < maxX && max.x > minX && min.y < maxY && max.y > minY && min.z < maxZ && max.z > minZ
    }

    /**
     * Perform an intersection operattion between
     * this and the given cuboid
     *
     * @param box The other cuboid
     * @return The intersection cuboid
     */
    fun intersection(box: Cuboid): Cuboid {
        val x1: Double = max(this.min.x, box.min.x)
        val y1: Double = max(this.min.y, box.min.y)
        val z1: Double = max(this.min.z, box.min.z)
        val x2: Double = min(this.max.x, box.max.x)
        val y2: Double = min(this.max.y, box.max.y)
        val z2: Double = min(this.max.z, box.max.z)

        return Cuboid(
            Position(x1, y1, z1),
            Position(x2, y2, z2)
        )
    }

    /**
     * Perform a union operation between
     * this and the given cuboid
     *
     * @param box The other cuboid
     * @return The intersection cuboid
     */
    fun union(box: Cuboid): Cuboid {
        val x1: Double = min(this.min.x, box.min.x)
        val y1: Double = min(this.min.y, box.min.y)
        val z1: Double = min(this.min.z, box.min.z)
        val x2: Double = max(this.max.x, box.max.x)
        val y2: Double = max(this.max.y, box.max.y)
        val z2: Double = max(this.max.z, box.max.z)

        return Cuboid(
            Position(x1, y1, z1),
            Position(x2, y2, z2)
        )
    }

    /**
     * Returns the cuboid width on the X axis
     *
     * @return double
     */
    val widthX: Double
        get() = abs(min.x - max.x)

    /**
     * Returns the cuboid width on the Z axis
     *
     * @return double
     */
    val widthZ: Double
        get() = abs(min.z - max.z)

    /**
     * Returns the cuboid height on the Y axis
     *
     * @return double
     */
    val height: Double
        get() = abs(min.y - max.y)

    /**
     * Return all positions contained within this cuboid
     *
     * @return Position list
     */
    fun contents(): MutableList<Position> {
        val content: MutableList<Position> = ArrayList()
        var x = min(min.x, max.x)
        while(x <= max(min.x, max.x)) {
            var y = min(min.y, max.y)
            while(y <= max(min.y, max.y)) {
                var z = min(min.z, max.z)
                while(z <= max(min.z, max.z)) {
                    content.add(Position(x, y, z))
                    z++
                }
                y++
            }
            x++
        }
        return content
    }

    /**
     * Returns true if the given Position is located within the bounds
     * of this cuboid.
     *
     * @param position Position
     * @return boolean
     */
    operator fun contains(position: Position): Boolean {
        val x = position.x
        val y = position.y
        val z = position.z
        val x1 = min(min.x, max.x)
        val y1 = min(min.y, max.y)
        val z1 = min(min.z, max.z)
        val x2 = max(min.x, max.x)
        val y2 = max(min.y, max.y)
        val z2 = max(min.z, max.z)

        return x in x1..x2 && y in y1..y2 && z in z1..z2
    }

    /**
     * Returns a random position from this Cuboid
     *
     * @return Random position
     */
    fun randomPosition(): Position {
        val random = ThreadLocalRandom.current()
        val deltaX = max.x - min.x
        val deltaY = max.y - min.y
        val deltaZ = max.z - min.z
        val rx = random.nextDouble() * deltaX + min.x
        val ry = random.nextDouble() * deltaY + min.y
        val rz = random.nextDouble() * deltaZ + min.z
        return Position(rx, ry, rz)
    }

    override fun toString(): String {
        return "Cuboid{" +
                "min=" + min +
                ", max=" + max +
                ", widthX=" + widthX +
                ", widthZ=" + widthZ +
                ", height=" + height +
                '}'
    }

    @Nonnull
    override fun iterator(): MutableIterator<Position> {
        return contents().iterator()
    }

    class Serializer : TypeAdapter<Cuboid>() {

        private val positionDelagate = Position.Serializer()

        override fun write(
            out: JsonWriter,
            value: Cuboid?
        ) {
            if(value == null) {
                out.nullValue()
                return
            }

            out.beginObject()
            out.name("min")
            positionDelagate.write(out, value.min)
            out.name("max")
            positionDelagate.write(out, value.max)
            out.endObject()
        }

        override fun read(inp: JsonReader): Cuboid? {
            if(inp.peek() == JsonToken.NULL) {
                inp.nextNull()
                return null
            }

            var min: Position? = null
            var max: Position? = null

            inp.beginObject()

            for(i in 0 until 2) {
                val name = inp.nextName()

                if(name == "min") {
                    min = positionDelagate.read(inp)
                } else if(name == "max") {
                    max = positionDelagate.read(inp)
                }
            }

            inp.endObject()

            if(min == null || max == null) {
                return null
            }

            return Cuboid(min, max)
        }

    }

    companion object {

        /**
         * Represents a fully empty cuboid
         */
        val EMPTY = Cuboid(
            Position(0, 0, 0),
            Position(0, 0, 0)
        )

    }

}