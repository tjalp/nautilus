package net.tjalp.nautilus.enchantment

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

interface NautilusEnchantment {

    val identifier: String
    val displayName: Component
    val maxLevel: Int

    fun conflicts(other: NautilusEnchantment): Boolean
    fun conflicts(other: Enchantment): Boolean

    companion object {

        val NAUTILUS_ENCHANTMENTS_PDC = NamespacedKey("nautilus", "custom_enchantments")
    }
}