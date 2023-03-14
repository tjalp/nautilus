package net.tjalp.aquarium.item.misc

import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.item.CustomItem
import net.tjalp.aquarium.registry.CUSTOM_ITEM
import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.ParticleEffect
import net.tjalp.nautilus.util.iterator.DirectionalLineIterator
import net.tjalp.aquarium.util.mini
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material.DEBUG_STICK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

object TestWandItem : CustomItem() {

    override val identifier: String = "test_wand"
    override val item: ItemStack
        get() = ItemBuilder(DEBUG_STICK)
            .name(mini("Test Wand"))
            .customModelData(1)
            .data(CUSTOM_ITEM, identifier)
            .build()

    override fun onUse(event: PlayerInteractEvent) {
        super.onUse(event)

        event.isCancelled = true

        val player = event.player
        val startPos = player.eyeLocation
        val line = DirectionalLineIterator(startPos, 1.0, startPos.direction, 100.0)
        val iterator = line.iterator()
        val random = ThreadLocalRandom.current()

        Aquarium.loader.server.scheduler.runTaskTimer(Aquarium.loader, Consumer {
            if (!iterator.hasNext()) {
                endLine(line.destination)
                it.cancel()
                return@Consumer
            }

            val loc = iterator.next()

            if (collides(loc)) {
                endLine(loc)
                it.cancel()
                return@Consumer
            }

            ParticleEffect(
                color = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)),
                count = 1
            ).play(loc)
        }, 0, 1)

        player.setCooldown(item.type, 80)
    }

    private fun endLine(location: Location) {
        location.world.createExplosion(location, 20f)
        location.world.strikeLightning(location)
    }

    private fun collides(location: Location): Boolean {

        if (location.block.isSolid) return true

        return false
    }
}