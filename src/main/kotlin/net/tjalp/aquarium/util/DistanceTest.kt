package net.tjalp.aquarium.util

import net.tjalp.aquarium.util.dimension.Cuboid
import net.tjalp.aquarium.util.dimension.Position
import org.bukkit.Location

/**
 * Utility class for performing efficient accurate distance checks
 *
 * @author Julian Mills
 */
class DistanceTest(alpha: Location, distance: Double) {

    private var distance = 0.0
    private var alpha: Location? = null
    private var cuboid: Cuboid? = null

    init {
        recompute(alpha, distance)
    }

    /**
     * Test if the given location is within range of this DistanceTest
     *
     * @param beta Location to test
     * @return Result
     */
    fun test(beta: Location): Boolean {
        return if(!cuboid!!.contains(Position(beta.x, beta.y, beta.z))) {
            false
        } else {
            alpha!!.distance(beta) <= distance
        }
    }

    /**
     * Recompute the boundary of this distance test
     *
     * @param alpha New alpha location
     * @param distance New distance from center
     */
    fun recompute(alpha: Location? = null, distance: Double = -1.0) {
        val newDistance = if(distance < 0) this.distance else distance
        val newAlpha = alpha ?: this.alpha ?: throw NullPointerException()

        if(this.alpha === newAlpha && this.distance == newDistance) {
            return
        }

        this.alpha = newAlpha
        this.distance = newDistance

        cuboid = Cuboid(
            Position(newAlpha.x - newDistance, newAlpha.y - newDistance, newAlpha.z - newDistance),
            Position(newAlpha.x + newDistance, newAlpha.y + newDistance, newAlpha.z + newDistance)
        )
    }
}