package net.tjalp.aquarium.item.bow

import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.item.CustomItem
import net.tjalp.aquarium.registry.CUSTOM_ITEM
import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.ParticleEffect
import net.tjalp.aquarium.util.mini
import org.bukkit.Material
import org.bukkit.Particle.*
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

object SupersonicBow : CustomItem() {

    override val identifier: String = "supersonic_bow"
    override val item: ItemStack
        get() = ItemBuilder(Material.BOW)
            .name(mini("Supersonic Bow"))
            .customModelData(3)
            .data(CUSTOM_ITEM, identifier)
            .build()

    override fun onShoot(event: EntityShootBowEvent) {
        super.onShoot(event)

        val projectile = event.projectile as Arrow

        if (!projectile.isCritical) return

        var isSuperSonic = false

        Aquarium.loader.server.scheduler.runTaskLater(Aquarium.loader, Consumer Task1@ {
            if (projectile.isDead || projectile.isInBlock) return@Task1

            val location = projectile.location

            projectile.velocity = projectile.velocity.clone().multiply(1.6)

            isSuperSonic = true

            ParticleEffect(SONIC_BOOM).play(location)
            ParticleEffect(
                effect = EXPLOSION_NORMAL, // poof
                speed = 0.16f,
                count = 50
            ).play(location)
            location.world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5f, 1.3f)
        }, 12)

        Aquarium.loader.server.scheduler.runTaskTimer(Aquarium.loader, Consumer Task2@ {
            if (projectile.isDead || projectile.isInBlock) {
                it.cancel()
                return@Task2
            }

            if (isSuperSonic) {
                ParticleEffect(SOUL_FIRE_FLAME).play(projectile.location)
            } else {
                ParticleEffect(effect = FLAME, speed = 0.015f).play(projectile.location)
                ParticleEffect(LAVA).play(projectile.location)
            }
        }, 1, 1)
    }
}