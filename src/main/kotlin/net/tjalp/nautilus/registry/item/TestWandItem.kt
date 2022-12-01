package net.tjalp.nautilus.registry.item

import net.tjalp.nautilus.util.ParticleEffect
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.item.NautilusItem
import net.tjalp.nautilus.util.iterator.DirectionalLineIterator
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

object TestWandItem : NautilusItem() {

    override val identifier = "test-wand"
    override val customModelData = null
    override val preferredMaterial = Material.DEBUG_STICK

    override fun onUse(event: PlayerInteractEvent) {
        super.onUse(event)

        event.isCancelled = true

        val nautilus = Nautilus.get()
        val player = event.player
        val startPos = player.eyeLocation
        val line = DirectionalLineIterator(startPos, 1.0, startPos.direction, 100.0)
        val iterator = line.iterator()
        val random = ThreadLocalRandom.current()

        nautilus.server.scheduler.runTaskTimer(nautilus, Consumer {
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
                count = 1,
                size = 7f
            ).play(loc)
        }, 0, 1)

        player.setCooldown(event.item?.type ?: return, 80)
    }

    private fun endLine(location: Location) {
        location.world.createExplosion(location, 5f)
    }

    private fun collides(location: Location): Boolean {
        return location.block.isSolid
    }
}