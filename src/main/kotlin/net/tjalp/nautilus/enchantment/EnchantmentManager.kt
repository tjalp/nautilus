package net.tjalp.nautilus.enchantment

import com.comphenix.protocol.PacketType.Play.Server.SET_SLOT
import com.comphenix.protocol.PacketType.Play.Server.WINDOW_ITEMS
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.jeff_media.morepersistentdatatypes.DataType
import com.jeff_media.morepersistentdatatypes.DataType.STRING_ARRAY
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.enchantment.NautilusEnchantment.Companion.NAUTILUS_ENCHANTMENTS_PDC
import net.tjalp.nautilus.registry.enchantment.ExplosiveEnchantment
import net.tjalp.nautilus.registry.enchantment.FireworkEnchantment
import net.tjalp.nautilus.util.ItemBuilder
import net.tjalp.nautilus.util.RomanNumeral
import net.tjalp.nautilus.util.builder
import net.tjalp.nautilus.util.register
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.STRING

/**
 * The enchantment manager manages everything that has to do
 * with (custom) enchantments.
 */
class EnchantmentManager(private val nautilus: Nautilus) {

    private val registeredEnchantments = mutableSetOf<NautilusEnchantment>()
    private val enchantmentsById = HashMap<String, NautilusEnchantment>()
    private val persistentDataType = DataType.asMap(STRING, INTEGER)

    init {
        EnchantmentListener().apply {
            register()
            nautilus.protocol.addPacketListener(this)
        }

        registerEnchantment(ExplosiveEnchantment)
//        registerEnchantment(FireworkEnchantment)
    }

    /**
     * Register a [NautilusEnchantment]
     *
     * @param enchantment The item to register
     */
    private fun registerEnchantment(enchantment: NautilusEnchantment) {
        check(enchantment.identifier !in this.enchantmentsById) { "Nautilus enchantment already registered" }

        this.registeredEnchantments += enchantment
        this.enchantmentsById[enchantment.identifier] = enchantment
    }

    /**
     * Get a [NautilusEnchantment] by the identifier
     *
     * @param id The identifier
     * @return This nautilus enchantment
     */
    fun getEnchantment(id: String): NautilusEnchantment? {
        return this.enchantmentsById[id.lowercase()]
    }

    /**
     * Whether a [NautilusEnchantment] with the specified id
     * exists.
     *
     * @param id The identifier to check
     * @return true if exists, false otherwise
     */
    fun enchantmentExists(id: String): Boolean {
        return id in this.enchantmentsById
    }

    fun enchant(item: ItemStack, enchantment: NautilusEnchantment, level: Int = 1) {
        val pdc = item.itemMeta.persistentDataContainer
        val map = pdc.get(NAUTILUS_ENCHANTMENTS_PDC, persistentDataType)?.toMutableMap() ?: mutableMapOf()

        ItemBuilder(item)
            .data(NAUTILUS_ENCHANTMENTS_PDC, persistentDataType, map.apply { this[enchantment.identifier] = level })
            .build()
    }

    fun hasEnchantment(item: ItemStack, enchantment: NautilusEnchantment): Boolean {
        val meta = if (item.hasItemMeta()) item.itemMeta else return false
        val pdc = meta.persistentDataContainer

        return pdc.get(NAUTILUS_ENCHANTMENTS_PDC, persistentDataType)?.containsKey(enchantment.identifier) == true
    }

    fun getEnchantmentLevel(item: ItemStack, enchantment: NautilusEnchantment): Int {
        val meta = if (item.hasItemMeta()) item.itemMeta else return 0
        val pdc = meta.persistentDataContainer

        return pdc.get(NAUTILUS_ENCHANTMENTS_PDC, persistentDataType)?.get(enchantment.identifier)?.coerceAtLeast(0) ?: 0
    }

    private inner class EnchantmentListener : Listener, PacketAdapter(nautilus, WINDOW_ITEMS, SET_SLOT) {

        override fun onPacketSending(event: PacketEvent) {
            when (event.packetType) {
                WINDOW_ITEMS -> this.handleWindowItems(event)
                SET_SLOT -> this.handleSetSlot(event)
            }
        }

        private fun handleWindowItems(event: PacketEvent) {
            val packet = event.packet
            val handle = packet.handle as ClientboundContainerSetContentPacket
            val carriedItem = packet.itemModifier.read(0)
            val items = packet.itemListModifier.read(0)
            val formatted = mutableListOf<ItemStack>()

            if (carriedItem.hasItemMeta() && carriedItem.itemMeta.persistentDataContainer.has(NAUTILUS_ENCHANTMENTS_PDC)) {
                packet.itemModifier.write(0, format(carriedItem))
            }

            for (item in items) {
                if (!item.hasItemMeta() || !item.itemMeta.persistentDataContainer.has(NAUTILUS_ENCHANTMENTS_PDC)) {
                    formatted += item
                    continue
                }

                formatted += format(item)
            }

            if (formatted == items) return

            packet.itemListModifier.write(0, formatted)
        }

        private fun handleSetSlot(event: PacketEvent) {
            val packet = event.packet
            val handle = packet.handle as ClientboundContainerSetSlotPacket
            val item = packet.itemModifier.read(0)

            if (!item.hasItemMeta() || !item.itemMeta.persistentDataContainer.has(NAUTILUS_ENCHANTMENTS_PDC)) return

            packet.itemModifier.write(0, format(item))
        }

        private fun format(item: ItemStack): ItemStack {
            val enchantmentIds = item.itemMeta.persistentDataContainer.get(
                NAUTILUS_ENCHANTMENTS_PDC,
                persistentDataType
            )?.keys ?: return item
            val enchantments = enchantmentIds.mapNotNull { getEnchantment(it) }
            val builder = item.builder()
            val lore = mutableListOf<Component>()
            val isEnchanted = item.enchantments.isNotEmpty() || enchantments.isNotEmpty()

            for (enchantment in enchantments) {
                val level = getEnchantmentLevel(item, enchantment)

                lore += enchantment.displayName.colorIfAbsent(GRAY)
                    .appendSpace().append(text(RomanNumeral.toRoman(level) ?: level.toString()))
            }
            for (enchantment in item.enchantments) {
                lore += translatable(enchantment.key.translationKey())
                    .color(GRAY)
                    .appendSpace()
                    .append(text(RomanNumeral.toRoman(enchantment.value) ?: enchantment.value.toString()))
            }

            return builder
                .flags(ItemFlag.HIDE_ENCHANTS)
                .lore(*lore.toTypedArray())
                .build()
        }
    }
}