package net.tjalp.nautilus.container

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Source.MASTER
import net.kyori.adventure.sound.Sound.sound
import org.bukkit.Sound.UI_BUTTON_CLICK
import org.bukkit.Material.AIR
import org.bukkit.inventory.ItemStack

/**
 * A blueprint specifies every single slot
 * in a container. Select a slot by using
 * [slot] and then use a method on it, such
 * as [set] or [item].
 */
class Blueprint(
    private val container: Container
) {

    private var currentSlot: Int = 0

    /**
     * Select a slot to use methods on.
     *
     * @param index The index of the slot to select.
     * @return self
     */
    fun slot(index: Int): Blueprint {
        this.currentSlot = index

        if (this.container.slotData[index] == null) this.container.slotData[index] = ContainerSlot()

        return this
    }

    /**
     * Set the current slot to an item that has
     * no special attributes.
     *
     * @param item The item to set.
     * @return self
     */
    fun item(item: ItemStack): Blueprint {
        this.container.inventory!!.setItem(this.currentSlot, item)
        this.container.slotData[this.currentSlot]!!.item = item
        return this
    }

    /**
     * Set the current slot to a [ContainerSlot].
     *
     * @param slot The container slot to set the current slot to.
     * @return self
     */
    fun set(slot: ContainerSlot): Blueprint {
        this.container.inventory!!.setItem(this.currentSlot, slot.item)
        this.container.slotData[this.currentSlot] = slot
        return this
    }

    /**
     * Make the current slot play a sound whenever it is clicked.
     * This does not overwrite the current item/container slot that
     * is in this location.
     *
     * @param sound The sound to play.
     * @return self
     */
    fun sound(sound: Sound): Blueprint {
        this.container.slotData[this.currentSlot]!!.clickSound = sound
        return this
    }

    /**
     * Clear the current slot and make it not do anything when clicked.
     *
     * @return self
     */
    fun clear(): Blueprint {
        this.container.inventory!!.setItem(this.currentSlot, ItemStack(AIR))
        this.container.slotData.remove(this.currentSlot)
        return this
    }

    /**
     * Make the current slot play a sound that is a 'click'.
     *
     * @return self
     */
    fun clickSound(): Blueprint {
        return this.sound(sound(UI_BUTTON_CLICK.key(), MASTER, 1f, 1f))
    }
}