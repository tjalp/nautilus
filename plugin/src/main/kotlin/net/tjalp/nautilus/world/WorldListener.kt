package net.tjalp.nautilus.world

import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent
import io.papermc.paper.event.player.PlayerBedFailEnterEvent
import net.tjalp.nautilus.util.register
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
import org.bukkit.entity.EnderDragon
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.BLINDNESS
import org.bukkit.potion.PotionEffectType.SLOW

/**
 * Listens for every change in the world and handles
 * accordingly.
 */
class WorldListener : Listener {

    init {
        register()
    }

    @EventHandler
    fun on(event: PlayerBedFailEnterEvent) {
        val bed = event.bed

        if (!bed.world.isBedWorks && bed.world.environment == World.Environment.THE_END) event.isCancelled = true
    }

    @EventHandler
    fun on(event: EntitySpawnEvent) {
        val entity = event.entity as? EnderDragon ?: return

        entity.getAttribute(GENERIC_MAX_HEALTH)?.baseValue = 1000.0 // why not :)
        entity.health = 1000.0
    }

    @EventHandler
    fun on(event: EnderDragonFireballHitEvent) {
        val entity = event.entity
        val cloud = event.areaEffectCloud

        cloud.radius = cloud.radius * 2
        cloud.radiusOnUse = 0f
        cloud.durationOnUse = 0
        cloud.reapplicationDelay = 5
        cloud.addCustomEffect(PotionEffect(SLOW, 10 * 20, 2), false)
        cloud.addCustomEffect(PotionEffect(BLINDNESS, 10 * 20, 0), false)
        cloud.particle = Particle.SNEEZE

        entity.location.createExplosion(event.entity, 5F, true, false)
    }
}