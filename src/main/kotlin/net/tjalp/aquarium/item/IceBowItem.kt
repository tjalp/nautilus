package net.tjalp.aquarium.item

import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.mini
import org.bukkit.Material.BOW
import org.bukkit.inventory.ItemStack

object IceBowItem : CustomItem() {

    override val identifier: String = "ice_bow"
    override val item: ItemStack
        get() = ItemBuilder(BOW).name(mini("Ice Bow")).customModelData(2).build()
}