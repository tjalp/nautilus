package net.tjalp.aquarium.registry

import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.item.CustomItem
import net.tjalp.aquarium.item.bow.BoomerangItem
import net.tjalp.aquarium.item.bow.IceBowItem
import net.tjalp.aquarium.item.bow.TribowItem
import net.tjalp.aquarium.item.horn.AirHornItem
import net.tjalp.aquarium.util.getCustomItem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent

class ItemRegistry {

    private val registry = HashMap<String, CustomItem>()

    val items: Set<CustomItem>
        get() = registry.values.toSet()

    init {
        Aquarium.loader.server.pluginManager.registerEvents(ItemListener(), Aquarium.loader)
    }

    /**
     * Get a [CustomItem] from an identifier
     *
     * @return The custom item linked to the specified identifier
     */
    fun getItem(identifier: String): CustomItem? {
        return registry[identifier]
    }

    /**
     * Register a [CustomItem] and add it to the registry
     */
    fun registerItem(item: CustomItem) {
        registry[item.identifier] = item
    }

    inner class ItemListener : Listener {

        @EventHandler
        fun onEntityShootBow(event: EntityShootBowEvent) {
            val item = event.bow ?: return
            val entity = event.entity
            val player = if (entity is Player) entity else return

            if (player.hasCooldown(item.type)) return

            item.getCustomItem()?.onShoot(event)
        }

        @EventHandler
        fun onEntityInteract(event: PlayerInteractEvent) {
            val item = event.item ?: return

            if (event.player.hasCooldown(item.type)) return

            item.getCustomItem()?.onUse(event)
        }
    }
}

fun registerItems(registry: ItemRegistry) {
    registry.registerItem(BoomerangItem)
    registry.registerItem(IceBowItem)
    registry.registerItem(TribowItem)

    registry.registerItem(AirHornItem)
}