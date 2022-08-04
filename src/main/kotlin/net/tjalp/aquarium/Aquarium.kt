package net.tjalp.aquarium

import me.neznamy.tab.api.TabAPI
import net.luckperms.api.LuckPerms
import net.tjalp.aquarium.listener.PlayerListener
import net.tjalp.aquarium.manager.DigManager
import net.tjalp.aquarium.manager.NametagManager
import net.tjalp.aquarium.util.register
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin


@Suppress("UNUSED")
class Aquarium : JavaPlugin() {

    /** The dig manager */
    lateinit var digManager: DigManager; private set

    /** The nametag manager */
    lateinit var nametagManager: NametagManager; private set

    /** The LuckPerms API */
    lateinit var luckperms: LuckPerms; private set

    override fun onEnable() {
        instance = this

        this.digManager = DigManager()
        this.nametagManager = NametagManager(this, TabAPI.getInstance())

        val lpProvider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        this.luckperms = lpProvider?.provider ?: return

        registerListeners()
        //server.pluginManager.registerEvents(TeamListener(), this)
    }

    private fun registerListeners() {
        PlayerListener(this).register()
    }

    override fun onDisable() {

    }

    companion object {

        /** The main [Aquarium] instance */
        lateinit var instance: Aquarium; private set
    }
}