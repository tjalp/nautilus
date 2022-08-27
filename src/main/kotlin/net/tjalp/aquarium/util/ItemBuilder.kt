package net.tjalp.aquarium.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class ItemBuilder {

    private var itemStack: ItemStack
    private var itemMeta: ItemMeta

    /**
     * Create a new builder from another builder
     *
     * @param builder The other builder
     */
    constructor(builder: ItemBuilder) {
        this.itemStack = builder.itemStack.clone()
        this.itemMeta = builder.itemMeta.clone()
    }

    /**
     * Create a new builder from a [Material] and amount ([Int])
     *
     * @param material The material
     * @param amount The amount
     */
    constructor(material: Material, amount: Int = 1): this(ItemStack(material, amount))

    /**
     * Create a new builder from an [ItemStack]
     */
    constructor(itemStack: ItemStack) {
        this.itemStack = itemStack.clone()
        this.itemMeta = this.itemStack.itemMeta
    }

    /**
     * Set the display name of the item
     *
     * @param name The display name to apply
     * @return self
     */
    fun name(name: Component?): ItemBuilder {
        this.itemMeta.displayName(name?.decoration(ITALIC, false))
        return this
    }

    /**
     * Add an enchantment to the item
     *
     * @param enchantment The enchantment to add
     * @param amplifier The enchantment's amplifier/level
     * @return self
     */
    fun enchant(enchantment: Enchantment, amplifier: Int): ItemBuilder {
        this.itemStack.addEnchantment(enchantment, amplifier)
        return this
    }

    /**
     * Remove an enchantment from the item
     *
     * @param enchantment The enchantment to remove
     * @return self
     */
    fun unEnchant(enchantment: Enchantment): ItemBuilder {
        this.itemStack.removeEnchantment(enchantment)
        return this
    }

    /**
     * Changes the material of the item stack
     *
     * @param material The material to change to
     * @return self
     */
    fun material(material: Material): ItemBuilder {
        this.itemStack.type = material
        return this
    }

    /**
     * Changes the amount of the item stack
     *
     * @param amount The new amount to change to
     * @return self
     */
    fun amount(amount: Int): ItemBuilder {
        this.itemStack.amount = amount
        return this
    }

    /**
     * Sets the item flags of the item stack
     *
     * @param flags The flags to set
     * @return self
     */
    fun flags(vararg flags: ItemFlag): ItemBuilder {
        for (flag in flags) if (this.itemStack.hasItemFlag(flag)) this.itemStack.removeItemFlags(flag)
        this.itemStack.addItemFlags(*flags)
        return this
    }

    /**
     * Makes this item breakable
     *
     * @param breakable Whether this item is breakable or not
     * @return self
     */
    fun breakable(breakable: Boolean = true): ItemBuilder {
        this.itemMeta.isUnbreakable = !breakable
        return this
    }

    /**
     * Makes this item unbreakable
     *
     * @param unbreakable Whether this item is unbreakable or not
     * @return self
     */
    fun unbreakable(unbreakable: Boolean = true): ItemBuilder {
        this.itemMeta.isUnbreakable = unbreakable
        return this
    }

    /**
     * Sets the durability of the item stack
     *
     * @param damage The damage to set
     * @return self
     */
    fun damage(damage: Int): ItemBuilder {
        val item = this.itemStack
        if (item is Damageable) item.damage = damage
        return this
    }

    /**
     * Sets the custom model data of this item stack
     *
     * @return self
     */
    fun customModelData(customModelData: Int?): ItemBuilder {
        this.itemMeta.setCustomModelData(customModelData)
        return this
    }

    /**
     * Set a persistent data value
     *
     * @param key The [NamespacedKey] to set the value of
     * @param type The [PersistentDataType] of the value
     * @param value The value to set
     * @return self
     */
    fun <T, Z : Any> data(key: NamespacedKey, type: PersistentDataType<T, Z>, value: Z): ItemBuilder {
        this.itemMeta.persistentDataContainer.set(key, type, value)
        return this
    }

    /**
     * Set a persistent data value with a string
     *
     * @param key The [NamespacedKey] to set the value of
     * @param value The value to set in a [String]
     * @return self
     */
    fun data(key: NamespacedKey, value: String): ItemBuilder {
        this.itemMeta.persistentDataContainer.set(key, PersistentDataType.STRING, value)
        return this
    }

    /**
     * Build the item builder
     *
     * @return The built [ItemStack]
     */
    fun build(): ItemStack {
        return this.itemStack.apply { itemMeta = this@ItemBuilder.itemMeta }
    }

    companion object {

        /**
         * Create an item builder with the item stack values
         *
         * @param itemStack The item stack to create an item builder of
         */
        fun copy(itemStack: ItemStack): ItemBuilder {
            return ItemBuilder(itemStack.clone())
        }
    }
}

fun ItemStack.builder(): ItemBuilder {
    return ItemBuilder.copy(this)
}