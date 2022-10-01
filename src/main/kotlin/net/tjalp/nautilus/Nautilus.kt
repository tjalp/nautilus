package net.tjalp.nautilus

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import net.luckperms.api.LuckPerms
import net.tjalp.nautilus.chat.ChatManager
import net.tjalp.nautilus.database.MongoManager
import net.tjalp.nautilus.exception.UnmetDependencyException
import net.tjalp.nautilus.player.profile.ProfileManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * The main class that contains all
 * registries and managers. An instance
 * of this class can be obtained via [Nautilus.get]
 */
class Nautilus : JavaPlugin() {

    /** The [ChatManager] instance */
    lateinit var chat: ChatManager; private set

    /** The [LuckPerms] API instance */
    lateinit var luckperms: LuckPerms; private set

    /** The Mongo Manager */
    lateinit var mongo: MongoManager; private set

    /** The [ProfileManager] */
    lateinit var profiles: ProfileManager; private set

    /** The [ProtocolManager] instance */
    lateinit var protocol: ProtocolManager; private set

    override fun onEnable() {
        instance = this

        val startTime = System.currentTimeMillis()

        this.chat = ChatManager(this)
        this.luckperms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider ?: throw UnmetDependencyException("LuckPerms cannot be found")
        this.mongo = MongoManager()
        this.profiles = ProfileManager(this)
        this.protocol = ProtocolLibrary.getProtocolManager() ?: throw UnmetDependencyException("ProtocolLib cannot be found")

        this.logger.info("Startup took ${System.currentTimeMillis() - startTime}ms!")
    }

    override fun onDisable() {
        this.mongo.dispose()
    }

    companion object {

        /** The main [Nautilus] instance */
        private lateinit var instance: Nautilus

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