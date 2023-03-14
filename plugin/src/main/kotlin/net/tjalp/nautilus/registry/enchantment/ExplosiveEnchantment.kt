package net.tjalp.nautilus.registry.enchantment

import net.kyori.adventure.text.Component
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.enchantment.NautilusEnchantment
import net.tjalp.nautilus.util.getEnchantmentLevel
import net.tjalp.nautilus.util.hasEnchantment
import net.tjalp.nautilus.util.register
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType.INTEGER

object ExplosiveEnchantment : NautilusEnchantment {

    private val EXPLOSIVE_PDC = NamespacedKey(nautilus, "explosive")
    private val nautilus; get() = Nautilus.get()

    init {
        ExplosiveEnchantmentListener().register()
    }

    override val identifier: String = "explosive"
    override val displayName: Component = Component.text("Explosive")
    override val maxLevel: Int = 1

    private class ExplosiveEnchantmentListener : Listener {

        @EventHandler
        fun on(event: EntityShootBowEvent) {
            val bow = event.bow ?: return

            if (!bow.hasEnchantment(ExplosiveEnchantment)) return

            event.projectile.persistentDataContainer.set(EXPLOSIVE_PDC, INTEGER, bow.getEnchantmentLevel(ExplosiveEnchantment))
        }

        @EventHandler
        fun on(event: ProjectileHitEvent) {
            val projectile = event.entity
            val shooter = projectile.shooter
            val location = projectile.location
            val level = projectile.persistentDataContainer.get(EXPLOSIVE_PDC, INTEGER)?.toFloat() ?: return

            projectile.remove()

            if (shooter !is Entity) {
                location.createExplosion(level, false, true)
                return
            }

            nautilus.server.scheduler.runTask(nautilus, Runnable {
                location.createExplosion(level, false, true)
            })
        }

        @EventHandler
        fun on(event: EntityDamageByEntityEvent) {
            val damager = event.damager

            if (damager !is LivingEntity) return

            val item = damager.equipment?.itemInMainHand ?: return

            if (!item.hasEnchantment(ExplosiveEnchantment)) return

            val level = item.getEnchantmentLevel(ExplosiveEnchantment).toFloat()

            nautilus.server.scheduler.runTask(nautilus, Runnable {
                event.entity.location.createExplosion(level, false, true)
            })
        }
    }
}