package net.tjalp.aquarium.util.iterator

import org.bukkit.Location

/**
 * An iterator that allows iteration over a straight line
 *
 * @author Julian Mills
 */
interface LineIterator : Iterable<Location> {

    /**
     * The position from which the iterator starts
     */
    val origin: Location

    /**
     * The destination of this iterator
     */
    val destination: Location

    /**
     * The step size of the iterator
     */
    val step: Double
}