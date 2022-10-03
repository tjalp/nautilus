package net.tjalp.nautilus.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.tjalp.nautilus.Nautilus
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

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