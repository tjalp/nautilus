package net.tjalp.nautilus

import cloud.commandframework.bukkit.CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION
import cloud.commandframework.bukkit.CloudBukkitCapabilities.BRIGADIER
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.jeff_media.customblockdata.CustomBlockData
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import net.tjalp.nautilus.block.BlockManager
import net.tjalp.nautilus.chat.ChatManager
import net.tjalp.nautilus.clan.ClanManager
import net.tjalp.nautilus.command.*
import net.tjalp.nautilus.config.NautilusConfig
import net.tjalp.nautilus.database.MongoManager
import net.tjalp.nautilus.enchantment.EnchantmentManager
import net.tjalp.nautilus.exception.UnmetDependencyException
import net.tjalp.nautilus.item.ItemManager
import net.tjalp.nautilus.ktor.ApiServer
import net.tjalp.nautilus.permission.PermissionManager
import net.tjalp.nautilus.player.Players
import net.tjalp.nautilus.player.disguise.DisguiseManager
import net.tjalp.nautilus.player.linking.GoogleLinkProvider
import net.tjalp.nautilus.player.mask.MaskManager
import net.tjalp.nautilus.player.profile.ProfileManager
import net.tjalp.nautilus.player.tag.NameTagManager
import net.tjalp.nautilus.registry.registerRanks
import net.tjalp.nautilus.registry.registerSuggestions
import net.tjalp.nautilus.scheduler.NautilusScheduler
import net.tjalp.nautilus.world.WorldListener
import net.tjalp.nautilus.world.claim.ClaimManager
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.geysermc.floodgate.api.FloodgateApi
import org.incendo.interfaces.paper.PaperInterfaceListeners
import org.ocpsoft.prettytime.PrettyTime
import java.util.function.Function

/**
 * The main class that contains all
 * registries and managers. An instance
 * of this class can be obtained via [Nautilus.get]
 */
class Nautilus : JavaPlugin() {

    /** The [BlockManager] instance */
    lateinit var blocks: BlockManager; private set

    /** The [ChatManager] instance */
    lateinit var chat: ChatManager; private set

    /** The [ClaimManager] instance */
    lateinit var claims: ClaimManager; private set

    /** The [ClanManager] instance */
    lateinit var clans: ClanManager; private set

    /** The command manager */
    lateinit var commands: PaperCommandManager<CommandSender>; private set

    /** The config */
    lateinit var config: NautilusConfig; private set

    /** The Disguise Manager */
    var disguises: DisguiseManager? = null; private set

    /** The Google Link Provider */
    lateinit var googleLinkProvider: GoogleLinkProvider; private set

    /** The floodgate API */
    var floodgate: FloodgateApi? = null; private set

    /** The HTTP client */
    val http = HttpClient(OkHttp)

    /** The Ktor Server */
    lateinit var apiServer: ApiServer; private set

    /** The Enchantment Manager */
    lateinit var enchantments: EnchantmentManager; private set

    /** The Item Manager */
    lateinit var items: ItemManager; private set

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

    /** The name tag manager */
    lateinit var nameTags: NameTagManager; private set

    override fun onEnable() {
        instance = this

        val startTime = System.currentTimeMillis()
        val useDisguises = server.pluginManager.isPluginEnabled("LibsDisguises")
        val useFloodgate = server.pluginManager.isPluginEnabled("floodgate")

        if (!useDisguises) this.logger.severe("LibsDisguises cannot be found, disguises will not work!")
        if (!useFloodgate) this.logger.severe("Floodgate cannot be found, Bedrock players will not get other inventories/forms")
        this.protocol =
            ProtocolLibrary.getProtocolManager() ?: throw UnmetDependencyException("ProtocolLib cannot be found")

        this.config = NautilusConfig.load(this)

        Players.initialize(this)

        this.scheduler = NautilusScheduler(this)
        this.mongo = MongoManager(this.logger, this.config.mongo)
        this.enchantments = EnchantmentManager(this)
        this.items = ItemManager(this)
        this.blocks = BlockManager(this)
        this.chat = ChatManager(this)
        this.claims = ClaimManager(this)
        this.clans = ClanManager(this)
        if (useDisguises) this.disguises = DisguiseManager(this)
        this.apiServer = ApiServer(this)
        this.perms = PermissionManager(this)
        this.profiles = ProfileManager(this)
        this.masking = MaskManager(this)
        this.nameTags = NameTagManager(this)
        if (useFloodgate) this.floodgate = FloodgateApi.getInstance()
        this.googleLinkProvider = GoogleLinkProvider(this)

        registerRanks(this)

        this.commands = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )
        if (this.commands.hasCapability(BRIGADIER)) this.commands.registerBrigadier()
        if (this.commands.hasCapability(ASYNCHRONOUS_COMPLETION)) this.commands.registerAsynchronousCompletions()

        registerSuggestions(this)

        ChunkCommand(this)
        ClanCommand(this)
        if (useDisguises) DisguiseCommand(this)
        HomeCommand(this)
        IdentityCommand(this)
        InspectCommand(this)
        MaskCommand(this)
        NautilusCommandImpl(this)
        NautilusEnchantmentCommand(this)
        NautilusItemCommand(this)
        PermissionsCommand(this)
        ProfileCommand(this)
        RealNameCommand(this)
        SpawnCommand(this)
        TeleportRequestCommand(this)
        WhisperCommand(this)

        // Register Paper interface listeners
        PaperInterfaceListeners.install(this)
        WorldListener()
        // Register block data listener
        CustomBlockData.registerListener(this)

        // Run task on server startup
        this.server.scheduler.runTask(this, Runnable {
            this.apiServer.start()
        })

        this.logger.info("Startup took ${System.currentTimeMillis() - startTime}ms!")
    }

    override fun onDisable() {
        this.apiServer.stop()
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