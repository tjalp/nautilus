package net.tjalp.nautilus.util

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