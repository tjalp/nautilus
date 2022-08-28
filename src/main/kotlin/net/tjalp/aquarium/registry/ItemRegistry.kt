package net.tjalp.aquarium.registry

import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.item.BoomerangItem
import net.tjalp.aquarium.item.CustomItem
import net.tjalp.aquarium.item.IceBowItem
import net.tjalp.aquarium.item.TribowItem
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.persistence.PersistentDataType.STRING

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
        fun onItem(event: EntityShootBowEvent) {
            val bow = event.bow ?: return
            val meta = bow.itemMeta ?: return
            val pdc = meta.persistentDataContainer

            if (!pdc.has(CUSTOM_ITEM)) return

            val identifier = pdc.get(CUSTOM_ITEM, STRING)!!
            val item = getItem(identifier) ?: return

            item.onShoot(event)
        }
    }
}

fun registerItems(registry: ItemRegistry) {
    registry.registerItem(BoomerangItem)
    registry.registerItem(IceBowItem)
    registry.registerItem(TribowItem)
}