package net.tjalp.nautilus.player.profile

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import com.google.gson.JsonParser
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.util.GsonHelper
import net.tjalp.nautilus.util.register
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.litote.kmongo.json
import org.litote.kmongo.reactivestreams.deleteOneById
import org.litote.kmongo.reactivestreams.findOneById
import java.time.Duration
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

        // todo remove this as it was a test
        (nautilus.server as CraftServer).handle.server.vanillaCommandDispatcher.dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSourceStack?>("profile")
                .then(argument<CommandSourceStack?, String?>("username", StringArgumentType.string())
                    .then(LiteralArgumentBuilder.literal<CommandSourceStack?>("delete")
                        .executes {
                            this.nautilus.scheduler.launch {
                                val username = it.getArgument("username", String::class.java)
                                val time = measureTimeMillis {
                                    val uniqueId = withContext(Dispatchers.IO) { nautilus.server.getPlayerUniqueId(username) ?: UUID.nameUUIDFromBytes(username.toByteArray()) }
                                    profiles.deleteOneById(uniqueId).awaitSingle()
                                }
                                it.source.sendSuccess(Component.literal("Deleted $username's profile (${time}ms)"), false)
                            }
                            return@executes Command.SINGLE_SUCCESS
                        })
                    .then(argument<CommandSourceStack?, String?>("data", StringArgumentType.greedyString())
                        .executes {
                            this.nautilus.scheduler.launch {
                                val username = it.getArgument("username", String::class.java)
                                val data = it.getArgument("data", String::class.java)
                                var profile: ProfileSnapshot?
                                val time = measureTimeMillis {
                                    profile = nautilus.profiles.profile(username)
                                    if (profile == null) {
                                        val uniqueId = withContext(Dispatchers.IO) { nautilus.server.getPlayerUniqueId(username) ?: UUID.nameUUIDFromBytes(username.toByteArray()) }
                                        profile = nautilus.profiles.createProfileIfNonexistent(uniqueId)
                                    }
                                    profile!!.data = data
                                    profile!!.save()
                                }
                                it.source.sendSuccess(Component.literal("Set data to ${profile!!.data} (${time}ms)"), false)
                            }
                            return@executes Command.SINGLE_SUCCESS
                        })
                    .executes {
                        this.nautilus.scheduler.launch {
                            val profile: ProfileSnapshot?
                            val time = measureTimeMillis {
                                profile = nautilus.profiles.profile(it.getArgument("username", String::class.java))
                            }
                            if (profile == null) {
                                it.source.sendSuccess(Component.literal("Profile does not exist (${time}ms)"), false)
                            } else {
                                val profileJson = GsonHelper.pretty().toJson(JsonParser.parseString(profile.json))
                                it.source.sendSuccess(Component.literal("Profile = $profileJson (${time}ms)"), false)
                            }
                        }
                        return@executes Command.SINGLE_SUCCESS
                    })
        )
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

            this.profiles.insertOne(profile).awaitSingle()
        }

        return profile
    }

    /**
     * Cache a profile
     *
     * @param profile The profile to cache
     */
    private fun cacheProfile(profile: ProfileSnapshot) {
        this.profileCache[profile.uniqueId] = profile
    }

    /**
     * The inner profile listener class, which will
     * listen and cache profiles
     */
    private inner class ProfileListener : Listener {

        @EventHandler
        fun on(event: AsyncPlayerPreLoginEvent) {
            synchronized(this) {
                nautilus.logger.info("Caching the profile of ${event.name} (${event.uniqueId})")

                runBlocking {
                    val time = measureTimeMillis {
                        cacheProfile(createProfileIfNonexistent(event.uniqueId))
                    }
                    nautilus.logger.info("Cached profile of ${event.name} (${time}ms)!")
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

        @EventHandler // todo remove: temporary to test random unique id
        fun on(event: PlayerSwapHandItemsEvent) {
            val player = event.player
            val uniqueId = UUID.randomUUID()
            val startTime = System.currentTimeMillis()

            nautilus.server.scheduler.runTaskAsynchronously(nautilus, Runnable {
                runBlocking {
                    player.playSound(player.location, Sound.UI_BUTTON_CLICK, 10f, 1f)
                    player.showTitle(Title
                        .title(
                            text("Please Wait", NamedTextColor.GRAY),
                            text("Fetching profile of $uniqueId...", TextColor.color(255, 191, 0)),
                            Times.times(Duration.ZERO, Duration.ofSeconds(5L), Duration.ofMillis(200L))
                        )
                    )

                    val profile = profile(uniqueId)

                    if (profile == null) {
                        player.sendMessage(text("No profile found (took ${System.currentTimeMillis() - startTime}ms)"))
                        player.clearTitle()
                        return@runBlocking
                    }

                    player.sendMessage(text("Profile uniqueId = ${profile.uniqueId}"))
                    player.clearTitle()
                }
            })
        }
    }
}