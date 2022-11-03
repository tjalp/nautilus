package net.tjalp.nautilus.container

import org.bukkit.Material.AIR
import org.bukkit.inventory.ItemStack

class ContainerSlot {

    var item: ItemStack
    var handler: ((ContainerClick) -> Unit)? = null

    constructor() {
        this.item = ItemStack(AIR)
    }

    constructor(item: ItemStack) {
        this.item = item
    }

    constructor(item: ItemStack, handler: (ContainerClick) -> Unit) {
        this.item = item
        this.handler = handler
    }

    companion object {

        val VOID = ContainerSlot()
    }
}