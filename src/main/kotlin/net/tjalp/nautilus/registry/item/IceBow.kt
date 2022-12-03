package net.tjalp.nautilus.registry.item

import net.tjalp.nautilus.util.ParticleEffect
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.item.NautilusItem
import net.tjalp.nautilus.util.register
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle.SNOWFLAKE
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType.INTEGER
import java.util.function.Consumer

object IceBow : NautilusItem() {

    private val nautilus = Nautilus.get()
    private val IS_ICICLE = NamespacedKey(nautilus, "isIcicle")

    override val identifier = "ice-bow"
    override val customModelData = 2
    override val preferredMaterial = Material.BOW

    init {
        IceBowListener().register()
    }

    override fun onShoot(event: EntityShootBowEvent) {
        super.onShoot(event)

        val projectile = event.projectile

        projectile.persistentDataContainer.set(IS_ICICLE, INTEGER, 1) // set to true

        nautilus.server.scheduler.runTaskTimer(nautilus, Consumer {
            if (projectile.isDead || (projectile is AbstractArrow && projectile.isInBlock)) {
                it.cancel()
                return@Consumer
            }

            ParticleEffect(SNOWFLAKE).play(projectile.location)
        }, 0, 1)
    }

    private class IceBowListener : Listener {

        @EventHandler
        fun on(event: EntityDamageByEntityEvent) {
            val projectile = event.damager

            if (projectile !is Projectile) return

            val entity = event.entity
            val isIcicle = projectile.persistentDataContainer.getOrDefault(IS_ICICLE, INTEGER, 0) == 1

            if (!isIcicle) return

            entity.freezeTicks = entity.maxFreezeTicks + (20 * 6) // 6 seconds, 120 ticks
        }
    }
}