package net.tjalp.nautilus.container

import net.kyori.adventure.text.Component
import net.tjalp.nautilus.util.register
import net.tjalp.nautilus.util.unregister
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory

abstract class Container(
    private var title: Component,
    private val rows: Int
) {

    internal val slotData: MutableMap<Int, ContainerSlot>
    internal var inventory: Inventory? = null
    private var viewer: Player? = null
    private val listener = ContainerListener()
    private var opened = false
    private val size
        get() = rows * 9

    init {
        this.slotData = hashMapOf()

        this.initialize()
    }

    private fun initialize() {
        this.inventory = Bukkit.createInventory(null, this.size, this.title)
    }

    fun open(player: Player) {
        if (this.opened) return

        if (this.inventory == null) initialize()

        this.opened = true
        this.viewer = player
        this.listener.register()

        this.render(player, Blueprint(this))

        player.openInventory(this.inventory!!)
    }

    fun close(vararg exempt: HumanEntity) {
        if (this.inventory == null) return

        this.listener.unregister()

        onClose(viewer!!)

        for (viewer in this.inventory!!.viewers.toTypedArray()) {
            if (viewer in exempt) continue
            viewer.closeInventory()
        }


        this.inventory = null
        this.viewer = null
        this.opened = false
    }

    fun rerender(modifier: Blueprint.(Blueprint) -> Unit) {
        val blueprint = Blueprint(this)
        modifier(blueprint, blueprint)
    }

    abstract fun render(player: Player, blueprint: Blueprint)

    open fun onClose(viewer: Player) {}

    private inner class ContainerListener : Listener {

        @EventHandler
        fun on(event: InventoryCloseEvent) {
            if (event.inventory == this@Container.inventory) this@Container.close(event.player)
        }

        @EventHandler
        fun on(event: PlayerQuitEvent) {
            if (event.player == this@Container.viewer) this@Container.close(event.player)
        }

        @EventHandler
        fun on(event: InventoryClickEvent) {
            if (event.inventory != this@Container.inventory || event.whoClicked !is Player) return

            if (event.click.isKeyboardClick || event.isShiftClick) {
                event.isCancelled = true
                return
            }

            val player = event.whoClicked as Player
            val slot = event.rawSlot

            if (slot !in 0 until size) return

            event.isCancelled = true

            val clickSlot = this@Container.slotData.getOrDefault(slot, null) ?: return
            val click = ContainerClick(event, player, clickSlot)

            clickSlot.handler?.invoke(click)
            clickSlot.clickSound?.let { player.playSound(it) }
        }

        @EventHandler
        fun on(event: InventoryDragEvent) {
            if (event.inventory != this@Container.inventory) return

            event.rawSlots.forEach { slot ->
                if (slot in 0 until size) event.isCancelled = true
            }
        }
    }
}