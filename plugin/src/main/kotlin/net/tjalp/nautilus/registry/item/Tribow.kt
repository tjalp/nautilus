package net.tjalp.nautilus.registry.item

import net.tjalp.nautilus.item.NautilusItem
import org.bukkit.Material.BOW
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

object Tribow : NautilusItem() {

    override val identifier = "tribow"
    override val customModelData = 1
    override val preferredMaterial = BOW

    override fun onShoot(event: EntityShootBowEvent) {
        super.onShoot(event)

        val projectile = event.projectile

        if (projectile !is Projectile) return

        // TODO: Actually make this work
        repeat(2) { index ->
            val velocity = projectile.velocity
            val simulated = if (index == 0) -10.0 else 10.0
            val quaternion = Quaternionf().setAngleAxis(simulated * 0.017453292, velocity.x, velocity.y, velocity.z)
            val vec = Vector3f(velocity.x.toFloat(), velocity.y.toFloat(), velocity.z.toFloat()).rotate(quaternion)
            val rotated = Vector(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())

            event.entity.launchProjectile(projectile.javaClass, rotated) { arrow ->
                if (arrow !is AbstractArrow) return@launchProjectile

                arrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY
            }
        }
    }
}