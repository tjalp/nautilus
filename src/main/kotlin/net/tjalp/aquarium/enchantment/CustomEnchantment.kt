package net.tjalp.aquarium.enchantment

import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

/**
 * Base class for custom enchantments which can be applied to item stacks
 */
abstract class CustomEnchantment(key: NamespacedKey) : Enchantment(key) {


}