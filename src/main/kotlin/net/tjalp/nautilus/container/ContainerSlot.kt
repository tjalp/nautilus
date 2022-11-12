package net.tjalp.nautilus.container

import net.kyori.adventure.sound.Sound
import org.bukkit.Material.AIR
import org.bukkit.inventory.ItemStack

class ContainerSlot {

    var item: ItemStack
    var handler: ((ContainerClick) -> Unit)? = null
    var clickSound: Sound? = null

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

    constructor(item: ItemStack, sound: Sound, handler: (ContainerClick) -> Unit) {
        this.item = item
        this.clickSound = sound
        this.handler = handler
    }

    companion object {

        val VOID = ContainerSlot()
    }
}