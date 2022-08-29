package net.tjalp.aquarium.util

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Particle.REDSTONE
import org.bukkit.entity.Player

/**
 * A single particle effect including additional
 * metadata such as count, offset, and speed.
 *
 * @author Julian Mills (thanks)
 */
open class ParticleEffect(
    var effect: Particle,
    var speed: Float = 0f,
    var count: Int = 1,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var offsetZ: Float = 0f,
    var data: Any? = null
) {

    /**
     * Create a new particle frame from the given frame
     *
     * @param effect Particle effect
     */
    constructor(effect: ParticleEffect) : this(
        effect.effect,
        effect.speed,
        effect.count,
        effect.offsetX,
        effect.offsetY,
        effect.offsetZ
    )

    /**
     * Create a particle effect with a given color
     */
    constructor(
        color: Color,
        size: Float = 1f,
        speed: Float = 0f,
        count: Int = 1,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        offsetZ: Float = 0f,
    ) : this(REDSTONE, speed, count, offsetX, offsetY, offsetZ, DustOptions(color, size))

    /**
     * Play a particle effect at the specified location
     *
     * @param loc Location
     */
    fun play(loc: Location) {
        loc.world!!.spawnParticle(
            effect,
            loc,
            count,
            offsetX.toDouble(),
            offsetY.toDouble(),
            offsetZ.toDouble(),
            speed.toDouble(),
            data,
            true
        )
    }

    /**
     * Play a particle effect at the specified location
     *
     * @param loc Location
     * @param radius Visibility radius
     */
    fun play(loc: Location, radius: Double) {
        val inRange = loc.world!!.players.stream()
            .filter { p: Player ->
                p.eyeLocation.distance(loc) <= radius
            }.toList().toTypedArray()

        play(loc, *inRange)
    }

    /**
     * Play a particle effect at the specified location for the given players
     *
     * @param loc Location
     */
    fun play(loc: Location, vararg players: Player) {
        for (player in players) {
            player.spawnParticle(
                effect,
                loc,
                count,
                offsetX.toDouble(),
                offsetY.toDouble(),
                offsetZ.toDouble(),
                speed.toDouble()
            )
        }
    }
}