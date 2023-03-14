package net.tjalp.nautilus.container

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.util.GroupedList
import net.tjalp.nautilus.util.ItemGenerator.clickable
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * A [Container] that can have multiple pages
 */
abstract class PageableContainer(
    title: Component,
    rows: Int,
    private val fillableSlots: IntArray,
    slots: List<ContainerSlot> = emptyList()
) : Container(title, rows) {

    private var slots: GroupedList<ContainerSlot>? = null
    private var currentPageIndex = 0

    constructor(title: Component, rows: Int, fillableSlots: IntRange, slots: List<ContainerSlot> = emptyList()) :
            this(title, rows, fillableSlots.toList().toIntArray(), slots)

    init {
//        this.slots = GroupedList(slots, fillableSlots.size)
    }

    override fun render(player: Player, blueprint: Blueprint) {
        this.rerender(false)
    }

    /**
     * Rerender the entire page
     */
    fun rerender(clean: Boolean) {
        this.rerender innerRerender@{ blueprint ->
            if (clean) this@PageableContainer.clear()

            if (currentPageIndex <= 0) blueprint.slot(size - 6).set(ContainerSlot.VOID)
            else blueprint.slot(size - 6).set(
                ContainerSlot(
                    clickable(
                        material = Material.ARROW,
                        name = text("Previous Page"),
                        description = text("Return to the previous page"),
                        clickTo = text("Move")
                    ).build()
                ) {
                    previous()
                }
            ).clickSound()

            if (currentPageIndex >= slots!!.groupCount - 1) blueprint.slot(size - 4).set(ContainerSlot.VOID)
            else blueprint.slot(size - 4).set(
                ContainerSlot(
                    clickable(
                        material = Material.ARROW,
                        name = text("Next Page"),
                        description = text("Continue on the next page"),
                        clickTo = text("Move")
                    ).build()
                ) {
                    next()
                }
            ).clickSound()

            val pageSlots = slots?.getGroup(currentPageIndex) ?: return@innerRerender

            for (slotIndex in fillableSlots.indices) {
                if (slotIndex >= pageSlots.size) blueprint.slot(fillableSlots[slotIndex]).set(ContainerSlot.VOID)
                else blueprint.slot(fillableSlots[slotIndex]).set(pageSlots[slotIndex])
            }
        }
    }

    /**
     * Clear the current filled slots for the page
     */
    fun clear() {
        this.rerender { blueprint ->
            for (slotIndex in fillableSlots) blueprint.slot(slotIndex).set(ContainerSlot.VOID)
        }
    }

    /**
     * Open the next page
     */
    fun next() {
        if (this.currentPageIndex >= this.slots!!.groupCount - 1) return
        this.currentPageIndex++
        this.rerender(true)
    }

    /**
     * Open the previous page
     */
    fun previous() {
        if (this.currentPageIndex <= 0) return
        this.currentPageIndex--
        this.rerender(true)
    }

    /**
     * Open a specific page
     */
    fun page(index: Int) {
        this.currentPageIndex = index.coerceIn(0 until this.slots!!.groupCount - 1)
        rerender(true)
    }

    /**
     * Set the slots
     */
    fun slots(slots: List<ContainerSlot>, rerender: Boolean = true) {
        this.slots = GroupedList(slots, fillableSlots.size)
        if (rerender) rerender(true)
    }
}