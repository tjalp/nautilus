package net.tjalp.aquarium

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.BukkitCommandManager
import cloud.commandframework.bukkit.CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION
import cloud.commandframework.bukkit.CloudBukkitCapabilities.BRIGADIER
import cloud.commandframework.execution.CommandExecutionCoordinator.simpleCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import me.neznamy.tab.api.TabAPI
import net.luckperms.api.LuckPerms
import net.tjalp.aquarium.listener.PlayerListener
import net.tjalp.aquarium.manager.ChunkManager
import net.tjalp.aquarium.manager.DigManager
import net.tjalp.aquarium.manager.NametagManager
import net.tjalp.aquarium.util.register
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function


@Suppress("UNUSED")
object Aquarium {

    /** The plugin loader (and [JavaPlugin] instance) */
    lateinit var loader: AquariumLoader; private set

    /** The chunk manager */
    lateinit var chunkManager: ChunkManager; private set

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
        this.chunkManager = ChunkManager()
        this.digManager = DigManager()
        this.nametagManager = NametagManager(this, TabAPI.getInstance())

        val lpProvider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        this.luckperms = lpProvider?.provider ?: return

        registerCommands()
        registerListeners()
    }

    private fun registerCommands() {
        // Register the command manager
        this.commands = PaperCommandManager(
            this.loader,
            simpleCoordinator(),
            Function.identity(),
            Function.identity()
        ).apply {
            if (hasCapability(BRIGADIER)) registerBrigadier()
            if (hasCapability(ASYNCHRONOUS_COMPLETION)) registerAsynchronousCompletions()
        }

        // Register annotations
        val commandMetaFunction =
            Function<ParserParameters, CommandMeta> { p: ParserParameters ->
                CommandMeta.simple()
                    .with(
                        CommandMeta.DESCRIPTION,
                        p.get(StandardParameters.DESCRIPTION, "No description")
                    )
                    .build()
            }
        val annotationParser = AnnotationParser(
            this.commands, /* Manager */
            CommandSender::class.java, /* Command sender type */
            commandMetaFunction /* Mapper for command meta instances */
        )

        annotationParser.parseContainers()
    }

    private fun registerListeners() {
        PlayerListener().register()
    }

    fun disable() {

    }
}