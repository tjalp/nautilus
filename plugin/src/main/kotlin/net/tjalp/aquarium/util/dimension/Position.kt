package net.tjalp.aquarium.util.dimension

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import net.tjalp.nautilus.util.dimension.Position
import kotlin.math.sqrt

/**
 * Represents a location in a world. This is a generic base object that acts as
 * a bukkit-like location. The Atlas plugin contains methods for converting
 * Position objects to Location objects.
 *
 * @author Julian Mills
 */
class Position {

    var x: Double
    var y: Double
    var z: Double
    var pitch = 0f
    var yaw = 0f

    /**
     * Create a new position with the specified x, y and z units.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    constructor(x: Int, y: Int, z: Int) {
        this.x = x.toDouble()
        this.y = y.toDouble()
        this.z = z.toDouble()
    }

    /**
     * Create a new position with the specified x, y and z units.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    constructor(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Create a new position with the specified x, y and z units. Besides the position,
     * the pitch and yaw is also given.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    constructor(x: Double, y: Double, z: Double, yaw: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.yaw = yaw
    }

    /**
     * Create a new position with the specified x, y and z units. Besides the position,
     * the pitch and yaw is also given.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    constructor(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.yaw = yaw
        this.pitch = pitch
    }

    /**
     * Get the squared distance between this position
     * and another
     *
     * @param pos The other position
     * @return Squared Distance
     */
    fun distance(pos: Position) : Double {
        return sqrt(distanceSquared(pos))
    }

    /**
     * Get the squared distance between this position
     * and another
     *
     * @param pos The other position
     * @return Distance * Distance
     */
    fun distanceSquared(pos: Position): Double {
        val xDiff = this.x - pos.x
        val yDiff = this.y - pos.y
        val zDiff = this.z - pos.z

        return (xDiff * xDiff) + (yDiff * yDiff) + (zDiff * zDiff)
    }

    /**
     * Take the floor from the current position
     */
    fun floor() =  Position(
        kotlin.math.floor(x),
        kotlin.math.floor(y),
        kotlin.math.floor(z)
    )

    /**
     * Take the ceil from the current position
     */
    fun ceil() =  Position(
        kotlin.math.ceil(x),
        kotlin.math.ceil(y),
        kotlin.math.ceil(z)
    )

    override fun toString(): String {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                '}'
    }

    class Serializer : TypeAdapter<Position>() {

        override fun write(
            out: JsonWriter,
            value: Position?
        ) {
            if(value == null) {
                out.nullValue()
                return
            }
            out.beginArray()
            out.value(value.x)
            out.value(value.y)
            out.value(value.z)
            out.value(value.pitch.toDouble())
            out.value(value.yaw.toDouble())
            out.endArray()
        }

        override fun read(`in`: JsonReader): Position? {
            if(`in`.peek() == JsonToken.NULL) {
                `in`.nextNull()
                return null
            }
            `in`.beginArray()
            val x = `in`.nextDouble()
            val y = `in`.nextDouble()
            val z = `in`.nextDouble()
            val pitch = `in`.nextDouble().toFloat()
            val yaw = `in`.nextDouble().toFloat()
            `in`.endArray()
            return Position(x, y, z, yaw, pitch)
        }
    }
}