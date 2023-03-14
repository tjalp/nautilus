package net.tjalp.aquarium.item

import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * This class must be implemented by custom items and be registered
 * as well via [net.tjalp.aquarium.Aquarium.items.registerItem]
 */
abstract class CustomItem {

    /**
     * The identifier this custom item has
     */
    abstract val identifier: String

    /**
     * The item to give
     */
    abstract val item: ItemStack

    /**
     * Called when this item is shot
     */
    open fun onShoot(event: EntityShootBowEvent) {}

    /**
     * Called when this item is used (by interacting with it)
     */
    open fun onUse(event: PlayerInteractEvent) {}
}