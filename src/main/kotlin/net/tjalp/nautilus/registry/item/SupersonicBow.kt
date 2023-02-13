package net.tjalp.nautilus.registry.item

import com.jeff_media.morepersistentdatatypes.DataType.BOOLEAN
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.item.NautilusItem
import net.tjalp.nautilus.util.ParticleEffect
import net.tjalp.nautilus.util.register
import org.bukkit.*
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import java.util.function.Consumer

object SupersonicBow : NautilusItem() {

    private val nautilus = Nautilus.get()
    private val IS_SUPER_SONIC_ARROW = NamespacedKey(nautilus, "isSuperSonicArrow")
    private val IS_SUPER_SONIC = NamespacedKey(nautilus, "isSuperSonic")

    override val identifier = "supersonic-bow"
    override val customModelData = 4
    override val preferredMaterial = Material.BOW

    init {
        SuperSonicBowListener().register()
    }

    override fun onShoot(event: EntityShootBowEvent) {
        super.onShoot(event)

        val projectile = event.projectile as AbstractArrow
        val nautilus = Nautilus.get()
        val scheduler = nautilus.server.scheduler
        var isSuperSonic = false

        projectile.knockbackStrength = projectile.knockbackStrength * 2
        projectile.persistentDataContainer.set(IS_SUPER_SONIC_ARROW, BOOLEAN, true)

        if (projectile.isCritical) {
            scheduler.runTaskLater(nautilus, Consumer Task1@{
                if (projectile.isDead || projectile.isInBlock) return@Task1

                val location = projectile.location

                projectile.velocity = projectile.velocity.clone().multiply(1.6)

                isSuperSonic = true
                projectile.persistentDataContainer.set(IS_SUPER_SONIC, BOOLEAN, true)

                ParticleEffect(Particle.SONIC_BOOM).play(location)
                ParticleEffect(
                    effect = Particle.EXPLOSION_NORMAL, // poof
                    speed = 0.16f,
                    count = 50
                ).play(location)
                location.world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5f, 1.3f)
            }, 12)
        }

        scheduler.runTaskTimer(nautilus, Consumer Task2@ {
            if (!projectile.isValid) {
                it.cancel()
                return@Task2
            }

            if (projectile.isInBlock) {
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

    private class SuperSonicBowListener : Listener {

        @EventHandler
        fun on(event: ProjectileHitEvent) {
            val projectile = event.entity
            val isSuperSonicArrow = projectile.persistentDataContainer.getOrDefault(IS_SUPER_SONIC_ARROW, BOOLEAN, false)

            if (!isSuperSonicArrow) return

            val isSuperSonic = projectile.persistentDataContainer.getOrDefault(IS_SUPER_SONIC, BOOLEAN, false)
            val velocity = event.hitBlockFace?.direction ?: projectile.velocity.multiply(-1)

            projectile.world.spawnEntity(projectile.location, EntityType.FIREWORK, CreatureSpawnEvent.SpawnReason.CUSTOM) { entity ->
                val firework = entity as Firework
                val color = if (isSuperSonic) Color.fromRGB(0, 255, 255) else Color.fromRGB(255,105,97)

                firework.fireworkMeta = firework.fireworkMeta.apply {
                    addEffect(
                        FireworkEffect.builder()
                            .withColor(color)
                            .with(FireworkEffect.Type.BURST)
                            .build()
                    )
                }
                firework.velocity = velocity
                firework.detonate()
            }

            projectile.remove()
        }
    }
}