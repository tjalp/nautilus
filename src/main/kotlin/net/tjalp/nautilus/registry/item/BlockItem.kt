package net.tjalp.nautilus.registry.item

import net.tjalp.nautilus.item.NautilusItem
import org.bukkit.Material

class BlockItem(
    override val identifier: String,
    override val customModelData: Int?
) : NautilusItem() {

    override val preferredMaterial = Material.NOTE_BLOCK
}