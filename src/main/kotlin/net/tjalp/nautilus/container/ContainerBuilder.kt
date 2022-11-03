package net.tjalp.nautilus.container

import net.tjalp.nautilus.Nautilus
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.CHEST
import org.bukkit.inventory.Inventory

class ContainerBuilder {

    private var type = CHEST
    private var rows = 1

    fun type(type: InventoryType): ContainerBuilder {
        this.type = type
        return this
    }

    fun rows(rows: Int): ContainerBuilder {
        this.rows = rows
        return this
    }

    fun blueprint(blueprint: Blueprint) {

    }

    fun build(): Inventory {
        val inventory = if (type == CHEST) Nautilus.get().server.createInventory(null, rows)
            else Nautilus.get().server.createInventory(null, type)



        return inventory
    }
}