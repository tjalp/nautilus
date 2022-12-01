package net.tjalp.nautilus.registry.item

import net.tjalp.nautilus.util.ParticleEffect
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.item.NautilusItem
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.AbstractArrow
import org.bukkit.event.entity.EntityShootBowEvent
import java.util.function.Consumer

object SupersonicBow : NautilusItem() {

    override val identifier = "supersonic-bow"
    override val customModelData = 3
    override val preferredMaterial = Material.BOW

    override fun onShoot(event: EntityShootBowEvent) {
        super.onShoot(event)

        val projectile = event.projectile as AbstractArrow
        val nautilus = Nautilus.get()
        val scheduler = nautilus.server.scheduler
        var isSuperSonic = false

        if (!projectile.isCritical) return

        projectile.knockbackStrength = projectile.knockbackStrength * 2

        scheduler.runTaskLater(nautilus, Consumer Task1@ {
            if (projectile.isDead || projectile.isInBlock) return@Task1

            val location = projectile.location

            projectile.velocity = projectile.velocity.clone().multiply(1.6)

            isSuperSonic = true

            ParticleEffect(Particle.SONIC_BOOM).play(location)
            ParticleEffect(
                effect = Particle.EXPLOSION_NORMAL, // poof
                speed = 0.16f,
                count = 50
            ).play(location)
            location.world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5f, 1.3f)
        }, 12)

        scheduler.runTaskTimer(nautilus, Consumer Task2@ {
            if (projectile.isDead || projectile.isInBlock) {
                it.cancel()
                return@Task2
            }

            if (isSuperSonic) {
                ParticleEffect(Particle.SOUL_FIRE_FLAME).play(projectile.location)
            } else {
                ParticleEffect(effect = Particle.FLAME, speed = 0.015f).play(projectile.location)
                ParticleEffect(Particle.LAVA).play(projectile.location)
            }
        }, 1, 1)
    }
}