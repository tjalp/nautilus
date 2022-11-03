package net.tjalp.nautilus.container

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

data class ContainerClick(
    val event: InventoryClickEvent,
    val player: Player,
    val slot: ContainerSlot
)