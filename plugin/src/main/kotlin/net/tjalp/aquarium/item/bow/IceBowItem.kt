package net.tjalp.aquarium.item.bow

import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.item.CustomItem
import net.tjalp.aquarium.registry.CUSTOM_ITEM
import net.tjalp.aquarium.registry.ICICLE
import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.ParticleEffect
import net.tjalp.aquarium.util.mini
import org.bukkit.Material.BOW
import org.bukkit.Particle.SNOWFLAKE
import org.bukkit.entity.Arrow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.INTEGER
import java.util.function.Consumer

object IceBowItem : CustomItem() {

    init {
        Aquarium.loader.server.pluginManager.registerEvents(ItemListener(), Aquarium.loader)
    }

    override val identifier: String = "ice_bow"
    override val item: ItemStack
        get() = ItemBuilder(BOW)
            .name(mini("Ice Bow"))
            .customModelData(2)
            .data(CUSTOM_ITEM, identifier)
            .build()

    override fun onShoot(event: EntityShootBowEvent) {
        super.onShoot(event)

        val projectile = event.projectile as Arrow

        projectile.persistentDataContainer.set(ICICLE, INTEGER, 1) // true

        Aquarium.loader.server.scheduler.runTaskTimer(Aquarium.loader, Consumer {
            if (projectile.isInBlock || projectile.isDead) {
                it.cancel()
                return@Consumer
            }

            ParticleEffect(SNOWFLAKE).play(projectile.location)
        }, 0, 1)
    }

    class ItemListener : Listener {

        @EventHandler
        fun onHit(event: ProjectileHitEvent) {
            val projectile = event.entity
            val entity = event.hitEntity ?: return
            val isIcicle = projectile.persistentDataContainer.getOrDefault(ICICLE, INTEGER, 0) == 1

            if (!isIcicle) return

            entity.freezeTicks = entity.maxFreezeTicks + (20 * 6) // 6 seconds, 120 ticks
        }
    }
}