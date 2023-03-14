package net.tjalp.nautilus.container

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * A container click is a simple data holder
 * of a click. This method is created on
 * user click in a container.
 *
 * @param event The click event.
 * @param player The player who clicked.
 * @param slot The slot that was clicked.
 */
data class ContainerClick(
    val event: InventoryClickEvent,
    val player: Player,
    val slot: ContainerSlot
)