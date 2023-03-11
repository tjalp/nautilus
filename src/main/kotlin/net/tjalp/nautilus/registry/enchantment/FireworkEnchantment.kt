package net.tjalp.nautilus.registry.enchantment

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.enchantment.NautilusEnchantment
import net.tjalp.nautilus.util.register
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Firework
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

object FireworkEnchantment : NautilusEnchantment {

    init {
        FireworkEnchantmentListener().register()
    }

    override val identifier: String = "firework"
    override val displayName: Component = text("Firework Enchantment")
    override val maxLevel: Int = 1

    private class FireworkEnchantmentListener : Listener {

        @EventHandler
        fun on(event: EntityDamageByEntityEvent) {
            val damager = event.damager

            if (damager !is LivingEntity) return

            val item = damager.equipment?.itemInMainHand ?: return

            if (!Nautilus.get().enchantments.hasEnchantment(item, FireworkEnchantment)) return

            event.entity.location.world.spawn(event.entity.location, Firework::class.java)
        }
    }
}