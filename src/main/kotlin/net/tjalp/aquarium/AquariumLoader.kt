package net.tjalp.aquarium

import org.bukkit.plugin.java.JavaPlugin

/**
 * The [Aquarium] loader, used to initialize the plugin
 */
class AquariumLoader : JavaPlugin() {

    override fun onEnable() = Aquarium.enable(this)

    override fun onDisable() = Aquarium.disable()
}