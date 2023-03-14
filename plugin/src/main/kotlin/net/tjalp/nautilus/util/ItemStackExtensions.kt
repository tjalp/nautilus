package net.tjalp.nautilus.util

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.enchantment.NautilusEnchantment
import org.bukkit.inventory.ItemStack

fun ItemStack.hasEnchantment(enchantment: NautilusEnchantment): Boolean {
    return Nautilus.get().enchantments.hasEnchantment(this, enchantment)
}

fun ItemStack.getEnchantmentLevel(enchantment: NautilusEnchantment): Int {
    return Nautilus.get().enchantments.getEnchantmentLevel(this, enchantment)
}