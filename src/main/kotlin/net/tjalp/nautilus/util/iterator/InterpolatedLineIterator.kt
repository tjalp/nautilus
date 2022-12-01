package net.tjalp.nautilus.util.iterator

import net.tjalp.aquarium.util.AquaMath
import org.bukkit.Location

/**
 *
 * The <tt>InterpolatedLineIterator</tt> iterates over a line knowing the
 * start and end position. The <tt>stepCount</tt> decides the amount of space
 * between each step.
 *
 * @author Julian Mills
 */
class InterpolatedLineIterator(
    override val origin: Location,
    override var destination: Location,
    val stepCount: Int
) : LineIterator {

    private var timeIterated = 0

    override val step: Double
        get() {
            return origin.distance(destination) / stepCount
        }

    override fun iterator(): Iterator<Location> {
        return IteratorImpl()
    }

    inner class IteratorImpl : Iterator<Location> {

        override fun hasNext(): Boolean {
            return timeIterated < stepCount
        }

        override fun next(): Location {
            if(timeIterated > stepCount) {
                throw IndexOutOfBoundsException()
            }

            val frac = timeIterated.toDouble() / stepCount

            timeIterated++

            return Location(
                origin.world,
                AquaMath.lerp(origin.x, destination.x, frac),
                AquaMath.lerp(origin.y, destination.y, frac),
                AquaMath.lerp(origin.z, destination.z, frac)
            )
        }

    }

}