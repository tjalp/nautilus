package net.tjalp.nautilus.item

import org.bukkit.NamespacedKey

/**
 * A Nautilus item is a custom item, usually with
 * a special custom model data value.
 */
abstract class NautilusItem {

    abstract val id: String

    companion object {

        val NAUTILUS_ITEM_ID_PDC = NamespacedKey("nautilus", "custom_item")
    }
}