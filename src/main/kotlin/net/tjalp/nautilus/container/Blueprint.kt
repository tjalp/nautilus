package net.tjalp.nautilus.container

import org.bukkit.Material.AIR
import org.bukkit.inventory.ItemStack

class Blueprint(
    private val container: Container
) {

    private var currentSlot: Int = 0

    fun slot(index: Int): Blueprint {
        this.currentSlot = index

        if (this.container.slotData[index] == null) this.container.slotData[index] = ContainerSlot()

        return this
    }

    fun item(item: ItemStack): Blueprint {
        this.container.inventory!!.setItem(this.currentSlot, item)
        this.container.slotData[this.currentSlot]!!.item = item
        return this
    }

    fun set(slot: ContainerSlot): Blueprint {
        this.container.inventory!!.setItem(this.currentSlot, slot.item)
        this.container.slotData[this.currentSlot] = slot
        return this
    }

    fun clear(): Blueprint {
        this.container.inventory!!.setItem(this.currentSlot, ItemStack(AIR))
        this.container.slotData.remove(this.currentSlot)
        return this
    }
}