package net.tjalp.nautilus.container

import net.kyori.adventure.sound.Sound
import org.bukkit.Material.AIR
import org.bukkit.inventory.ItemStack

/**
 * A container slot is a slot that is bound
 * to a container. It must hold an item and
 * a handler can be applied to it.
 */
class ContainerSlot {

    /** The item stack that this slot must display */
    var item: ItemStack

    /** The handler that is called when this slot is clicked */
    var handler: ((ContainerClick) -> Unit)? = null

    /** The sound that is played when this slot is clicked */
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