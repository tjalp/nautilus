package net.tjalp.nautilus.player.profile

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import com.google.gson.JsonParser
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextColor.color
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.util.*
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.litote.kmongo.json
import org.litote.kmongo.reactivestreams.deleteOneById
import org.litote.kmongo.reactivestreams.findOneById
import org.litote.kmongo.reactivestreams.save
import org.litote.kmongo.setValue
import java.time.LocalDateTime
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * The profile manager manages everything about
 * profiles and everything they contain, like
 * prefixes, suffixes, ranks, roles etc.
 */
class ProfileManager(
    private val nautilus: Nautilus
) {

    /** The profile cache that can be accessed via [cacheProfile] */
    private val profileCache = HashMap<UUID, ProfileSnapshot>()

    /** The coroutine Mongo client */
    private val profiles = MongoCollections.profiles

    init {
        ProfileListener().register()
    }

    /**
     * Retrieve the latest [ProfileSnapshot] of an
     * online player. If not online, an error will
     * be thrown.
     *
     * @param player The player to retrieve the profile of
     * @return The associated & cached [ProfileSnapshot] of the specified player
     */
    fun profile(player: Player): ProfileSnapshot {
        check(player.isOnline) { "Player must be online to retrieve profile" }

        return this.profileCache[player.uniqueId] ?: run {
            throw IllegalStateException("No profile present for ${player.uniqueId}")
        }
    }

    /**
     * Retrieve the latest [ProfileSnapshot] of an
     * (offline) user from their unique id.
     * This method is suspending, so handle it accordingly.
     *
     * @param uniqueId The unique identifier of the target user
     * @return The associated & cached [ProfileSnapshot] of the specified unique id
     */
    suspend fun profile(uniqueId: UUID): ProfileSnapshot? {
        val player = this.nautilus.server.getPlayer(uniqueId)

        if (player != null) return this.profile(player)

        return this.profiles.findOneById(uniqueId).awaitFirstOrNull()
    }

    /**
     * Retrieve the latest [ProfileSnapshot] of an
     * (offline) user from their username.
     * This method is suspending, so handle it accordingly.
     *
     * @param username The username of the target user
     * @return The associated & cached [ProfileSnapshot] of the specified username
     */
    suspend fun profile(username: String): ProfileSnapshot? {
        val player = this.nautilus.server.getPlayer(username)

        if (player != null) return this.profile(player)

        val uniqueId = withContext(Dispatchers.IO) {
            nautilus.server.getPlayerUniqueId(username)
        } ?: return null

        return this.profile(uniqueId)
    }

    /**
     * Create a new profile if no profile exists for
     * the target unique id
     */
    suspend fun createProfileIfNonexistent(uniqueId: UUID): ProfileSnapshot {
        var profile = this.profile(uniqueId)

        if (profile == null) {
            profile = ProfileSnapshot(uniqueId)

            this.profiles.save(profile).awaitSingle()
        }

        return profile
    }

    /**
     * Update a profile with a new one
     *
     * @param profile The new profile to update with
     */
    fun onProfileUpdate(profile: ProfileSnapshot) {
        var previous: ProfileSnapshot? = null

        if (profile.player()?.isOnline == true) {
            previous = cacheProfile(profile)
        }

        ProfileUpdateEvent(
            profile = profile,
            previous = previous
        ).callEvent()
    }

    /**
     * Cache a profile
     *
     * @param profile The profile to cache
     * @return The previous profile if exists, otherwise null
     */
    private fun cacheProfile(profile: ProfileSnapshot): ProfileSnapshot? {
        nautilus.logger.info("Caching the profile of ${profile.player()?.name} (${profile.uniqueId})")
        return this.profileCache.put(profile.uniqueId, profile)
    }

    /**
     * The inner profile listener class, which will
     * listen and cache profiles
     */
    private inner class ProfileListener : Listener {

        @EventHandler
        fun on(event: AsyncPlayerPreLoginEvent) {
            synchronized(this) {
                runBlocking {
                    cacheProfile(createProfileIfNonexistent(event.uniqueId))
                }
            }
        }

        @EventHandler
        fun on(event: PlayerConnectionCloseEvent) {
            synchronized(this) {
                nautilus.logger.info("Removing the cached profile of ${event.playerName} (${event.playerUniqueId})")
                profileCache -= event.playerUniqueId
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        fun on(event: PlayerJoinEvent) {
            val player = event.player
            val profile = player.profile()

            player.displayName(profile.nameComponent(showSuffix = false))

            nautilus.scheduler.launch {
                profile.update(
                    setValue(ProfileSnapshot::lastKnownName, player.name),
                    setValue(ProfileSnapshot::lastOnline, LocalDateTime.now())
                )
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        fun on(event: PlayerQuitEvent) {
            val player = event.player
            val profile = player.profile()

            nautilus.scheduler.launch {
                profile.update(setValue(ProfileSnapshot::lastOnline, LocalDateTime.now()))
            }
        }

        @EventHandler // todo remove: temporary to test random unique id
        fun on(event: PlayerSwapHandItemsEvent) {
            val player = event.player
            val uniqueId = UUID.randomUUID()
            val startTime = System.currentTimeMillis()

            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 10f, 1f)
            player.sendActionBar(
                text().append(text("Please Wait: ", GRAY))
                    .append(text("Fetching profile of $uniqueId...", color(255, 191, 0)))
            )

            nautilus.scheduler.launch {
                val profile = profile(uniqueId)

                if (profile == null) {
                    player.sendMessage(text("No profile found (took ${System.currentTimeMillis() - startTime}ms)"))
                    delay(500)
                    player.sendActionBar(text(""))
                    return@launch
                }

                player.sendMessage(text("Profile uniqueId = ${profile.uniqueId}"))
                delay(500)
                player.sendActionBar(text(""))
            }
        }
    }
}