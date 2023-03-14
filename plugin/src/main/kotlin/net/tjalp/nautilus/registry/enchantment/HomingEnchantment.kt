package net.tjalp.nautilus.registry.enchantment

import com.jeff_media.morepersistentdatatypes.DataType.INTEGER
import com.jeff_media.morepersistentdatatypes.DataType.UUID
import net.kyori.adventure.text.Component
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.enchantment.NautilusEnchantment
import net.tjalp.nautilus.util.getEnchantmentLevel
import net.tjalp.nautilus.util.hasEnchantment
import net.tjalp.nautilus.util.register
import org.bukkit.NamespacedKey
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import java.util.function.Consumer
import kotlin.math.pow

object HomingEnchantment : NautilusEnchantment {

    private val nautilus; get() = Nautilus.get()
    private val HOMING_TARGET_PDC = NamespacedKey(nautilus, "homing_target")
    private val HOMING_LEVEL_PDC = NamespacedKey(nautilus, "homing_level")

    init {
        HomingEnchantmentListener().register()
    }

    override val identifier: String = "homing"
    override val displayName: Component = Component.text("Homing")
    override val maxLevel: Int = 3

    private class HomingEnchantmentListener : Listener {

        @EventHandler
        fun on(event: EntityShootBowEvent) {
            val bow = event.bow ?: return
            val projectile = event.projectile

            if (!bow.hasEnchantment(HomingEnchantment)) return

            projectile.persistentDataContainer.set(HOMING_LEVEL_PDC, INTEGER, bow.getEnchantmentLevel(HomingEnchantment))

            nautilus.server.scheduler.runTaskTimer(nautilus, Consumer {
                if (!projectile.isValid || projectile.isDead) {
                    it.cancel()
                    return@Consumer
                }

                findTargetAndSetVelocity(projectile)
            }, 3, 1)
        }

        private fun findTargetAndSetVelocity(projectile: Entity) {
            var target = projectile.persistentDataContainer.get(HOMING_TARGET_PDC, UUID)?.let {
                projectile.world.getEntity(it) as? LivingEntity
            }
            val level = projectile.persistentDataContainer.getOrDefault(HOMING_LEVEL_PDC, INTEGER, 1)

            if (target == null) {
                target = projectile.world.rayTraceEntities(projectile.location, projectile.velocity, 75.0 + 25*level, 2.5) {
                    isValid(projectile, it)
                }?.hitEntity as? LivingEntity
            }

            if (target == null) {
                target = projectile.location.getNearbyLivingEntities(25.0) {
                    isValid(projectile, it)
                }.minByOrNull { it.location.distanceSquared(projectile.location) }
            }

            if (target == null) {
                projectile.setGravity(true)
                projectile.persistentDataContainer.remove(HOMING_TARGET_PDC)
                return
            }

            projectile.setGravity(false)
            projectile.persistentDataContainer.set(HOMING_TARGET_PDC, UUID, target.uniqueId)

            val targetVec = target.boundingBox.center
            val location = projectile.location.toVector()
            val vector = targetVec.subtract(location)

            projectile.velocity = projectile.velocity.multiply(0.90.pow(level)).add(vector.normalize().multiply(0.25 * level))
        }

        private fun isValid(projectile: Entity, target: Entity): Boolean {
            if (target !is LivingEntity) return false
            if (target == projectile) return false
            if (projectile is AbstractArrow && projectile.shooter == target) return false
            return target.hasLineOfSight(projectile)
        }
    }
}