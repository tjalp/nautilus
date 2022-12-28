package net.tjalp.nautilus.registry.item

import com.jeff_media.morepersistentdatatypes.DataType.BOOLEAN
import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.item.CraftableItem
import net.tjalp.nautilus.item.NautilusItem
import net.tjalp.nautilus.util.ItemBuilder
import net.tjalp.nautilus.util.ParticleEffect
import net.tjalp.nautilus.util.register
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle.SNOWFLAKE
import org.bukkit.Sound
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.recipe.CraftingBookCategory.EQUIPMENT
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.BLINDNESS
import java.util.function.Consumer
import kotlin.math.roundToInt

object IceBow : NautilusItem(), CraftableItem {

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

        projectile.persistentDataContainer.set(IS_ICICLE, BOOLEAN, true)

        nautilus.server.scheduler.runTaskTimer(nautilus, Consumer {
            if (!projectile.isValid) {
                it.cancel()
                return@Consumer
            }

            if (projectile is AbstractArrow && projectile.isInBlock) return@Consumer

            ParticleEffect(SNOWFLAKE).play(projectile.location)
        }, 0, 1)
    }

    private class IceBowListener : Listener {

        @EventHandler
        fun on(event: EntityDamageByEntityEvent) {
            val projectile = event.damager

            if (projectile !is Projectile) return

            val entity = event.entity
            val isIcicle = projectile.persistentDataContainer.getOrDefault(IS_ICICLE, BOOLEAN, false)

            if (!isIcicle) return

            entity.freezeTicks = entity.maxFreezeTicks + (20 * 6) // 6 seconds, 120 ticks
            entity.world.playSound(entity.location, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f)
            if (entity is LivingEntity) entity.addPotionEffect(PotionEffect(BLINDNESS, 10, 255, false, false, false))

            val boundingBox = entity.boundingBox

            ParticleEffect(
                effect = SNOWFLAKE,
                speed = 0.05f,
                count = (boundingBox.volume.roundToInt() * 20).coerceAtMost(250),
                offsetX = boundingBox.widthX.toFloat() / 2,
                offsetY = boundingBox.height.toFloat() / 2,
                offsetZ = boundingBox.widthZ.toFloat() / 2
            ).play(boundingBox.center.toLocation(entity.world))
        }
    }

    override fun recipe(): Recipe {
        return ShapedRecipe(
            NamespacedKey(this.nautilus, this.identifier),
            ItemBuilder(this.preferredMaterial)
                .name(text("Ice Bow"))
                .customModelData(this.customModelData)
                .data(NAUTILUS_ITEM_ID_PDC, this.identifier)
                .build()
        ).shape(
            "iii",
            "ibi",
            "iii"
        ).setIngredient('i', Material.ICE).setIngredient('b', Material.BOW).apply {
            category = EQUIPMENT
        }
    }
}