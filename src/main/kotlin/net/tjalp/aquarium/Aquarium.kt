package net.tjalp.aquarium

import cloud.commandframework.bukkit.BukkitCommandManager
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import me.neznamy.tab.api.TabAPI
import net.luckperms.api.LuckPerms
import net.tjalp.aquarium.listener.PlayerListener
import net.tjalp.aquarium.manager.DigManager
import net.tjalp.aquarium.manager.NametagManager
import net.tjalp.aquarium.util.register
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function


@Suppress("UNUSED")
object Aquarium {

    /** The plugin loader (and [JavaPlugin] instance) */
    lateinit var loader: AquariumLoader; private set

    /** The dig manager */
    lateinit var digManager: DigManager; private set

    /** The nametag manager */
    lateinit var nametagManager: NametagManager; private set

    /** The LuckPerms API */
    lateinit var luckperms: LuckPerms; private set

    /** The command manager */
    lateinit var commands: BukkitCommandManager<CommandSender>; private set

    fun enable(loader: AquariumLoader) {
        this.loader = loader
        this.digManager = DigManager()
        this.nametagManager = NametagManager(this, TabAPI.getInstance())

        val lpProvider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        this.luckperms = lpProvider?.provider ?: return

        this.commands = PaperCommandManager(
            this.loader,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        ).apply {
            if (hasCapability(CloudBukkitCapabilities.BRIGADIER)) registerBrigadier()
        }

        registerCommands()
        registerListeners()
    }

    private fun registerCommands() {
        commands.command(commands.commandBuilder("yeet"))
    }

    private fun registerListeners() {
        PlayerListener().register()
    }

    fun disable() {

    }
}