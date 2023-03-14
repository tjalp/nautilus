package net.tjalp.nautilus.item

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.registry.item.*
import net.tjalp.nautilus.util.register
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

/**
 * The Item Manager manages everything that has to do
 * with (custom) items.
 */
class ItemManager(private val nautilus: Nautilus) {

    private val registeredItems = mutableSetOf<NautilusItem>()
    private val itemsById = HashMap<String, NautilusItem>()

    init {
        ItemListener().register()

        // Register all items
        registerItem(HomingBow)
        registerItem(IceBow)
        registerItem(SupersonicBow)
        registerItem(TestWandItem)
        registerItem(Tribow)
    }

    /**
     * Register a [NautilusItem]
     *
     * @param item The item to register
     */
    fun registerItem(item: NautilusItem) {
        check(item.identifier !in this.itemsById) { "Nautilus item already registered" }

        this.registeredItems += item
        this.itemsById[item.identifier] = item

        if (item is CraftableItem) this.nautilus.server.addRecipe(item.recipe())
    }

    /**
     * Get a [NautilusItem] by the identifier
     *
     * @param id The identifier
     * @return This nautilus item
     */
    fun getItem(id: String): NautilusItem {
        return this.itemsById[id.lowercase()] ?: throw IllegalArgumentException("Nautilus item by id $id does not exist!")
    }

    /**
     * Get a [NautilusItem] by the item.
     *
     * @param item The item to check.
     * @return The [NautilusItem], or null if nonexistent.
     */
    fun getItem(item: ItemStack): Array<NautilusItem> {
        val list = mutableListOf<NautilusItem>()
        for (id in this.identifiers(item)) {
            list += this.getItem(id)
        }
        return list.toTypedArray()
    }

    /**
     * Whether a [NautilusItem] with the specified id
     * exists.
     *
     * @param id The identifier to check
     * @return true if exists, false otherwise
     */
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
        return this.identifiers(item).contains(nautilusItem.identifier)
    }

    /**
     * Get the identifier of an item stack, which
     * may be null.
     */
    fun identifiers(item: ItemStack): Array<String> {
        val meta = item.itemMeta
        val pdc = meta.persistentDataContainer

        return arrayOf(pdc.get(NautilusItem.NAUTILUS_ITEM_ID_PDC, STRING) ?: return emptyArray())
    }

    /**
     * Get a set of all the [NautilusItem]s that
     * are registered. The set is immutable.
     *
     * @return An immutable set of [NautilusItem]s
     */
    fun items(): Set<NautilusItem> = this.registeredItems.toSet()

    private inner class ItemListener : Listener {

        @EventHandler
        fun on(event: PlayerInteractEvent) {
            ntlItems(event.item).forEach { it.onUse(event) }
        }

        @EventHandler
        fun on(event: EntityShootBowEvent) {
            ntlItems(event.bow).forEach { it.onShoot(event) }
        }

        private fun ntlItems(item: ItemStack?): Array<NautilusItem> {
            return getItem(item ?: return emptyArray())
        }
    }
}