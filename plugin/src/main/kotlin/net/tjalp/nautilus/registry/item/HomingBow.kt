package net.tjalp.nautilus.registry.item

import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.item.NautilusItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityShootBowEvent
import java.util.function.Consumer

object HomingBow : NautilusItem() {

    override val identifier = "homing-bow"
    override val customModelData = 3
    override val preferredMaterial = Material.BOW

    override fun onShoot(event: EntityShootBowEvent) {
        super.onShoot(event)

        val projectile = event.projectile as Projectile
        projectile.setGravity(false)
        val shooter = projectile.shooter
        val nautilus = Nautilus.get()
        val scheduler = nautilus.server.scheduler

        scheduler.runTaskTimer(nautilus, Consumer {
            if (projectile.isDead) {
                it.cancel()
                return@Consumer
            }

            val targets = projectile.getNearbyEntities(50.0, 50.0, 50.0)
                .sortedBy { projectile.location.distance((shooter as Entity).location) }
                .iterator()
            var finalTarget: LivingEntity? = null

            while (targets.hasNext()) {
                val target = targets.next()

                if (target !is LivingEntity || target == projectile.shooter) continue
                else {
                    finalTarget = target
                    break
                }
            }

            if (finalTarget == null) return@Consumer

            Bukkit.broadcast(text("Closest target: ${finalTarget.name}"))

            val vector = finalTarget.boundingBox.center.clone().subtract(projectile.location.toVector())

            projectile.velocity = projectile.velocity.clone().add(vector.multiply(0.02))
        }, 5, 1)
    }
}