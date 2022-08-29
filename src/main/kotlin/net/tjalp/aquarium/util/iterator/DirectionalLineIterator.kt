package net.tjalp.aquarium.util.iterator

import org.bukkit.Location
import org.bukkit.util.Vector

/**
 *
 * The <tt>DirectionalLineIterator</tt> iterates over a line knowing the
 * start, step amount and max line length.
 *
 * @author Julian Mills
 */
class DirectionalLineIterator(
    override val origin: Location,
    override val step: Double,
    private val direction: Vector,
    private val maxLength: Double = 50.0
) : LineIterator {

    private var currentPoint = 0.5

    init {
        direction.normalize()
    }

    override val destination: Location
        get() {
            direction.multiply(maxLength - step) // multiply
            origin.add(direction) // add
            val curr = origin.clone()
            origin.subtract(direction) // subtract
            direction.normalize() // normalize
            return curr
        }

    override fun iterator(): Iterator<Location> {
        return IteratorImpl()
    }

    inner class IteratorImpl : Iterator<Location> {

        override fun hasNext(): Boolean {
            return currentPoint < maxLength
        }

        override fun next(): Location {
            direction.multiply(currentPoint) // multiply
            origin.add(direction) // add
            val curr = origin.clone()
            currentPoint += step
            origin.subtract(direction) // subtract
            direction.normalize() // normalize
            return curr
        }

    }

}