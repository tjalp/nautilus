package net.tjalp.aquarium.item.horn

import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.sound
import net.tjalp.aquarium.item.CustomItem
import net.tjalp.aquarium.registry.CUSTOM_ITEM
import net.tjalp.aquarium.util.DistanceTest
import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.ParticleEffect
import net.tjalp.aquarium.util.iterator.DirectionalLineIterator
import net.tjalp.aquarium.util.mini
import org.bukkit.Material.GOAT_HORN
import org.bukkit.Particle.EXPLOSION_NORMAL
import org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP
import org.bukkit.Sound.ITEM_TRIDENT_RIPTIDE_3
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

object AirHornItem : CustomItem() {

    override val identifier: String = "air_horn"
    override val item: ItemStack
        get() = ItemBuilder(GOAT_HORN)
            .name(mini("Air Horn"))
            .customModelData(1)
            .data(CUSTOM_ITEM, identifier)
            .build()

    override fun onUse(event: PlayerInteractEvent) {
        super.onUse(event)

        val player = event.player
        val startPos = player.eyeLocation
        val lit = DirectionalLineIterator(player.location, 0.1, player.location.direction, 5.5)
        val distanceTest = DistanceTest(lit.destination, 6.0)

        repeat(50) {
            val lookingPos = player.location

            lookingPos.yaw += (Math.random().toFloat() - 0.5f) * 40f
            lookingPos.pitch += (Math.random().toFloat() - 0.5f) * 40f

            val lookingDir = lookingPos.direction

            ParticleEffect(
                effect = EXPLOSION_NORMAL,
                speed = 0.4f + (Math.random().toFloat() * 0.8f),
                count = 0,
                offsetX = lookingDir.x.toFloat(),
                offsetY = lookingDir.y.toFloat(),
                offsetZ = lookingDir.z.toFloat()
            ).play(startPos)
        }

        startPos.world.playSound(sound(key(ITEM_TRIDENT_RIPTIDE_3.key.key), Sound.Source.MASTER, 1f, 0.6f), Sound.Emitter.self())
        startPos.world.playSound(sound(key(ENTITY_ENDER_DRAGON_FLAP.key.key), Sound.Source.MASTER, 2f, 0.6f), Sound.Emitter.self())

        player.velocity = player.velocity.clone().add(player.location.direction.normalize().multiply(-0.8))

        val targets = startPos.world.entities.filter {
            if (it == player) return@filter false

            distanceTest.test(it.location)
        }

        for (target in targets) {
            if (target.isOnGround) {
                target.velocity = target.velocity.setY(0.5)
            }

            val velocity = player.location.direction.normalize().multiply(2.0)
            val vector = Vector(velocity.x, velocity.y.coerceAtMost(1.5), velocity.z)

            if (target is LivingEntity && target.isGliding) {
                vector.multiply(2f)
            }

            target.velocity = target.velocity.clone().add(vector)
        }

        player.setCooldown(item.type, 200)
    }
}