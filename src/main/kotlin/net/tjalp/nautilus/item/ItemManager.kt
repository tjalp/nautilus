package net.tjalp.nautilus.item

import net.tjalp.nautilus.Nautilus
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * The Item Manager manages everything that has to do
 * with (custom) items.
 */
class ItemManager(private val nautilus: Nautilus) {

    private val registeredItems = mutableSetOf<NautilusItem>()
    private val itemsById = HashMap<String, NautilusItem>()

    fun registerItem(item: NautilusItem) {
        check(item.id !in this.itemsById) { "Nautilus item already registered" }

        this.registeredItems += item
        this.itemsById[item.id] = item
    }

    fun getItem(id: String): NautilusItem {
        return this.itemsById[id.lowercase()] ?: throw IllegalArgumentException("Nautilus item by id $id does not exist!")
    }

    fun itemExists(id: String): Boolean {
        return id in this.itemsById
    }

    /**
     * Whether this item is this specific
     * nautilus item.
     *
     * @param item The [ItemStack] to check
     * @return Whether it is this nautilus item
     */
    fun isItem(item: ItemStack, nautilusItem: NautilusItem): Boolean {
        val meta = item.itemMeta
        val pdc = meta.persistentDataContainer

        return pdc.get(NautilusItem.NAUTILUS_ITEM_ID_PDC, PersistentDataType.STRING) == nautilusItem.id
    }
}