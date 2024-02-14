package net.tjalp.nautilus.registry.enchantment

import com.destroystokyo.paper.MaterialTags
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.enchantment.NautilusEnchantment
import net.tjalp.nautilus.util.ParticleEffect
import net.tjalp.nautilus.util.hasEnchantment
import net.tjalp.nautilus.util.register
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.GameMode.*
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.util.Vector

object BoosterEnchantment : NautilusEnchantment {

    private val nautilus; get() = Nautilus.get()

    init {
        BoosterEnchantmentListener().register()
    }

    override val identifier: String = "booster"
    override val displayName: Component = text("Booster")
    override val maxLevel: Int = 1

    override fun isCompatible(material: Material): Boolean {
        return MaterialTags.BOOTS.isTagged(material)
    }

    private class BoosterEnchantmentListener : Listener {

        @EventHandler
        fun on(event: PlayerArmorChangeEvent) {
            val player = event.player
            val hasEnchantment = event.newItem.hasEnchantment(BoosterEnchantment)
            val isBoots = event.slotType == PlayerArmorChangeEvent.SlotType.FEET

            if (!isBoots || player.gameMode == CREATIVE || player.gameMode == SPECTATOR) return

            event.player.allowFlight = hasEnchantment
        }

        @EventHandler
        fun on(event: PlayerToggleFlightEvent) {
            val player = event.player
            val boots = player.inventory.boots ?: return

            if (!event.isFlying || !boots.hasEnchantment(BoosterEnchantment) || player.gameMode == CREATIVE || player.gameMode == SPECTATOR) return

            event.isCancelled = true

            val direction = player.location.direction
            val velocity = Vector(direction.x, player.velocity.y.plus(0.7).coerceAtMost(0.7), direction.z)

            player.velocity = velocity
            ParticleEffect(Particle.EXPLOSION_NORMAL, 0.12f, 25).play(player.location)
            player.world.playSound(player.location, Sound.ENTITY_ZOMBIE_INFECT, 0.8f, 2f)
        }
    }
}