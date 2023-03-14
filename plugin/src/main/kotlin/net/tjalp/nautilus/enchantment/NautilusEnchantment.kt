package net.tjalp.nautilus.enchantment

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

/**
 * A custom enchantment that can be applied to
 * items in the game. The enchantment level itself
 * is stored in the item's persistent data container,
 * not the enchantment itself.
 */
interface NautilusEnchantment {

    /**
     * The unique identifier of this enchantment.
     * Note: Changing this will cause items with the previous name
     * to no longer function.
     */
    val identifier: String

    /**
     * The display name of this enchantment that is displayed
     * in the lore (description) of an item stack.
     */
    val displayName: Component

    /**
     * The (natural) maximum level of this enchantment. This
     * value can be overridden.
     */
    val maxLevel: Int

    /**
     * Whether this enchantment conflicts with another [NautilusEnchantment].
     *
     * @param other The other enchantment
     * @return true if the enchantments conflict, false otherwise
     */
    fun conflicts(other: NautilusEnchantment): Boolean = false

    /**
     * Whether this enchantment conflicts with an [Enchantment].
     *
     * @param other The other enchantment
     * @return true if the enchantments conflict, false otherwise
     */
    fun conflicts(other: Enchantment): Boolean = false

    /**
     * Whether this enchantment can be applied to the given [Material].
     *
     * @param material The material to check
     * @return true if the enchantment can be applied, false otherwise
     */
    fun isCompatible(material: Material): Boolean = true

    companion object {

        val NAUTILUS_ENCHANTMENTS_PDC = NamespacedKey("nautilus", "custom_enchantments")
    }
}