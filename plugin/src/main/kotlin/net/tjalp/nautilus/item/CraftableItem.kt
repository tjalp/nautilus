package net.tjalp.nautilus.item

import org.bukkit.inventory.Recipe

interface CraftableItem {

    fun recipe(): Recipe
}