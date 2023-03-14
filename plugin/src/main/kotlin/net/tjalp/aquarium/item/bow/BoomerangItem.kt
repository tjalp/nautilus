package net.tjalp.aquarium.item.bow

import net.tjalp.aquarium.item.CustomItem
import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.mini
import org.bukkit.Material.BOW
import org.bukkit.inventory.ItemStack

object BoomerangItem : CustomItem() {

    override val identifier: String = "boomerang"
    override val item: ItemStack
        get() = ItemBuilder(BOW).name(mini("Boomerang")).customModelData(3).build()
}