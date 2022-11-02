package net.tjalp.nautilus

import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import net.tjalp.nautilus.chat.ChatManager
import net.tjalp.nautilus.command.DisguiseCommand
import net.tjalp.nautilus.command.MaskCommand
import net.tjalp.nautilus.command.NautilusCommandImpl
import net.tjalp.nautilus.command.ProfileCommand
import net.tjalp.nautilus.database.MongoManager
import net.tjalp.nautilus.exception.UnmetDependencyException
import net.tjalp.nautilus.permission.PermissionManager
import net.tjalp.nautilus.player.Players
import net.tjalp.nautilus.player.disguise.DisguiseManager
import net.tjalp.nautilus.player.mask.MaskManager
import net.tjalp.nautilus.player.profile.ProfileManager
import net.tjalp.nautilus.player.tag.NametagManager
import net.tjalp.nautilus.registry.registerRanks
import net.tjalp.nautilus.scheduler.NautilusScheduler
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.ocpsoft.prettytime.PrettyTime
import java.util.function.Function

/**
 * The main class that contains all
 * registries and managers. An instance
 * of this class can be obtained via [Nautilus.get]
 */
class Nautilus : JavaPlugin() {

    /** The [ChatManager] instance */
    lateinit var chat: ChatManager; private set

    /** The command manager */
    lateinit var commands: PaperCommandManager<CommandSender>; private set

    /** The Disguise Manager */
    lateinit var disguises: DisguiseManager; private set

    /** The HTTP client */
    val http = HttpClient(OkHttp)

    /** The Mongo Manager */
    lateinit var mongo: MongoManager; private set

    /** The [PermissionManager] */
    lateinit var perms: PermissionManager; private set

    /** The [ProfileManager] */
    lateinit var profiles: ProfileManager; private set

    /** The [ProtocolManager] instance */
    lateinit var protocol: ProtocolManager; private set

    /** The scheduler of Nautilus */
    lateinit var scheduler: NautilusScheduler; private set

    /** The [MaskManager] */
    lateinit var masking: MaskManager; private set

    /** The nametag manager */
    lateinit var nametags: NametagManager; private set

    override fun onEnable() {
        instance = this

        val startTime = System.currentTimeMillis()

        if (!server.pluginManager.isPluginEnabled("LibsDisguises")) {
            throw UnmetDependencyException("LibsDisguises cannot be found")
        }
        this.protocol = ProtocolLibrary.getProtocolManager() ?: throw UnmetDependencyException("ProtocolLib cannot be found")

        Players.initialize(this)

        this.chat = ChatManager(this)
        this.disguises = DisguiseManager(this)
        this.mongo = MongoManager()
        this.perms = PermissionManager(this)
        this.profiles = ProfileManager(this)
        this.scheduler = NautilusScheduler(this)
        this.masking = MaskManager(this)
        this.nametags = NametagManager(this)

        registerRanks(this)

        this.commands = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )
        if (this.commands.hasCapability(CloudBukkitCapabilities.BRIGADIER)) this.commands.registerBrigadier()
        if (this.commands.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) this.commands.registerAsynchronousCompletions()

        DisguiseCommand(this)
        MaskCommand(this)
        NautilusCommandImpl(this)
        ProfileCommand(this)

        this.logger.info("Startup took ${System.currentTimeMillis() - startTime}ms!")
    }

    override fun onDisable() {
        this.mongo.dispose()
    }

    companion object {

        /** The main [Nautilus] instance */
        private lateinit var instance: Nautilus

        val TIME_FORMAT = PrettyTime()

        /**
         * Gets the main [Nautilus] instance.
         *
         * Note: Please prefer to pass the instance instead of
         * using this method. This method should only be used
         * in cases where it is impossible to pass an instance.
         */
        fun get(): Nautilus = this.instance
    }
}