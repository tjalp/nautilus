package net.tjalp.nautilus.util

import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.util.RGBLike
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

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
        this.itemStack = itemStack
        this.itemMeta = this.itemStack.itemMeta.clone()
    }

    /**
     * Set the display name of the item
     *
     * @param name The display name to apply
     * @return self
     */
    fun name(name: Component?): ItemBuilder {
        this.itemMeta.displayName(name?.decoration(ITALIC, name.hasDecoration(ITALIC)))
        return this
    }

    /**
     * Set the lore of the item
     *
     * @param lore The lore to apply
     * @return self
     */
    fun lore(vararg lore: Component?): ItemBuilder {
        val decoratedLore = lore.map {
            it?.colorIfAbsent(WHITE)?.decoration(ITALIC, it.hasDecoration(ITALIC))
        }
        this.itemMeta.lore(decoratedLore.toList())
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
        this.itemStack.addUnsafeEnchantment(enchantment, amplifier)
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
        val meta = this.itemMeta
        for (flag in flags) if (meta.hasItemFlag(flag)) meta.removeItemFlags(flag)
        meta.addItemFlags(*flags)
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
     * Set the player profile of the skull
     *
     * @param username The username of the player to set
     * @return self
     */
    fun skull(username: String): ItemBuilder {
        val meta = this.itemMeta
        if (meta is SkullMeta) {
            val profile = Bukkit.getServer().createProfile(username)
            meta.playerProfile = profile
        }
        return this
    }

    /**
     * Set the player profile of the skull
     *
     * @param skin The skin to set
     * @return self
     */
    fun skull(skin: SkinBlob): ItemBuilder {
        val meta = this.itemMeta
        if (meta is SkullMeta) {
            val profile = Bukkit.createProfile(UUID.randomUUID()).apply {
                setProperty(ProfileProperty("textures", skin.value, skin.signature))
            }
            meta.playerProfile = profile
        }
        return this
    }

    /**
     * Set the color of the [Dyeable]
     */
    fun color(color: RGBLike): ItemBuilder {
        val meta = this.itemMeta
        if (meta is LeatherArmorMeta) {
            meta.setColor(org.bukkit.Color.fromRGB(color.red(), color.green(), color.blue()))
        }
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

        /**
         * Create an item builder of the current item stack
         * Note: This method does not [ItemStack.clone] the
         * current item, so everything is applied directly
         * to the specified item stack
         *
         * @param itemStack The item stack to create an item builder of
         */
        fun of(itemStack: ItemStack): ItemBuilder {
            return ItemBuilder(itemStack)
        }
    }
}

fun ItemStack.builder(): ItemBuilder {
    return ItemBuilder.copy(this)
}