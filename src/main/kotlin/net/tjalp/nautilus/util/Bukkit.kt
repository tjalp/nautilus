package net.tjalp.nautilus.util

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentIteratorType.DEPTH_FIRST
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.minimessage.MiniMessage
import net.tjalp.nautilus.Nautilus
import org.bukkit.Chunk
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.map.MinecraftFont

const val MAX_MOTD_WIDTH = 258

/**
 * Center a component
 *
 * @param component The component to center
 * @param maxWidth The max width of what to center
 * @return The centered component
 */
fun centerComponent(component: Component, maxWidth: Int): Component {
    val font = MinecraftFont.Font
    var pixels = 0
    val iterator = component.iterator(DEPTH_FIRST)

    while (iterator.hasNext()) {
        val itComponent = iterator.next()

        if (itComponent !is TextComponent) continue

        for (char in itComponent.content().toCharArray()) {
            var width = font.getWidth(char.toString()) + 1 // Letter spacing

            if (char != ' ' && itComponent.hasDecoration(BOLD)) width += 1

            pixels += width
        }
    }

    if (pixels > 0) pixels-- // Remove trailing space pixel

    val halvedLength = maxWidth / 2
    val halvedPixels = pixels / 2
    val toCompensate = halvedLength - halvedPixels
    val spacePixels = font.getWidth(" ") + 1 // Letter spacing of 1 pixel
    var compensated = 0
    val builder = StringBuilder()

    while (compensated < toCompensate) {
        builder.append(" ")
        compensated += spacePixels
    }

    return text(builder.toString()).append(component)
}

/**
 * Utility method to register a listener
 */
fun Listener.register() {
    val nautilus = Nautilus.get()

    nautilus.server.pluginManager.registerEvents(this, nautilus)
}

/**
 * Utility method to unregister a listener
 */
fun Listener.unregister() = HandlerList.unregisterAll(this)

/**
 * Format a string using MiniMessage
 *
 * @param value The string to convert
 * @return A new [Component] generated from the specified string
 */
fun mini(value: String): Component = MiniMessage.miniMessage().deserialize(value)

/**
 * See [mini]
 */
fun String.component(): Component = mini(this)

/**
 * Receive a [SkinBlob] from a [PlayerProfile]
 */
fun PlayerProfile.skin(): SkinBlob? {
    val textures = this.properties.firstOrNull { it.name == "textures" } ?: return null
    val signature = textures.signature ?: return null
    val value = textures.value

    return SkinBlob(value, signature)
}

/**
 * Set a skin of a [PlayerProfile]
 */
fun PlayerProfile.skin(skin: SkinBlob) {
    this.setProperty(ProfileProperty("textures", skin.value, skin.signature))
}

/**
 * Get the neighboring chunks of a chunk
 */
fun Chunk.neighbors(): Set<Chunk> {
    val neighbors = mutableSetOf<Chunk>()

    neighbors.add(this.world.getChunkAt(this.x + 1, this.z))
    neighbors.add(this.world.getChunkAt(this.x - 1, this.z))
    neighbors.add(this.world.getChunkAt(this.x, this.z + 1))
    neighbors.add(this.world.getChunkAt(this.x, this.z - 1))

    return neighbors
}