package net.tjalp.nautilus.item

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A Nautilus item is a custom item, usually with
 * a special custom model data value.
 */
abstract class NautilusItem {

    abstract val identifier: String

    abstract val customModelData: Int?

    abstract val preferredMaterial: Material

    open fun onUse(event: PlayerInteractEvent) {}
    open fun onShoot(event: EntityShootBowEvent) {}

    companion object {

        val NAUTILUS_ITEM_ID_PDC = NamespacedKey("nautilus", "custom_item")
    }
}