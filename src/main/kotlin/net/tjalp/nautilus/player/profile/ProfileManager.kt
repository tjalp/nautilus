package net.tjalp.nautilus.player.profile

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.event.ProfileLoadEvent
import net.tjalp.nautilus.event.ProfileUnloadEvent
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.util.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.litote.kmongo.`in`
import org.litote.kmongo.regex
import org.litote.kmongo.setValue
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.text.RegexOption.IGNORE_CASE

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
     * Retrieve the latest [ProfileSnapshot] of a
     * cached player. If not cached, an error will
     * be thrown.
     *
     * @param player The player to retrieve the profile of
     * @return The associated & cached [ProfileSnapshot] of the specified player
     */
    fun profile(player: Player): ProfileSnapshot {
        return this.profileCache[player.uniqueId] ?: run {
            throw IllegalStateException("No profile present for ${player.uniqueId}")
        }
    }

    /**
     * Retrieve the latest **cached** [ProfileSnapshot] of a
     * unique identifier.
     *
     * @param uniqueId The unique id of the cached profile
     * @return The profile if cached, otherwise null
     */
    fun profileIfCached(uniqueId: UUID): ProfileSnapshot? = this.profileCache[uniqueId]

    /**
     * Retrieve the latest **cached** [ProfileSnapshot] of a
     * username.
     *
     * @param username The username of the cached profile
     * @return The profile if cached, otherwise null
     */
    fun profileIfCached(username: String): ProfileSnapshot? {
        val player = this.nautilus.server.getPlayerExact(username) ?: return null

        return this.profileIfCached(player.uniqueId)
    }

    /**
     * Retrieve all profiles of multiple unique ids. If a
     * cached version of the profile is available, it will
     * be returned instead of the most up-to-date version.
     *
     * @param uniqueIds The unique ids to get the profiles of
     * @return An array of all [ProfileSnapshot]s.
     */
    suspend fun profiles(vararg uniqueIds: UUID): Array<ProfileSnapshot> {
        val uniqueIdList = uniqueIds.toMutableList()
        val profiles = mutableListOf<ProfileSnapshot>()

        for (id in uniqueIds) {
            val profile = profileIfCached(id) ?: continue

            uniqueIdList -= id
            profiles += profile
        }

        if (uniqueIdList.isNotEmpty()) {
            profiles += this.profiles.find(ProfileSnapshot::uniqueId `in` uniqueIdList).toList()
        }

        return profiles.toTypedArray()
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

        return this.profiles.findOneById(uniqueId)
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
        val player = this.nautilus.server.getPlayerExact(username)

        if (player != null) return this.profile(player)

        val profiles = this.profiles.find(
            ProfileSnapshot::lastKnownName regex Regex("^${username.escapeIfNeeded()}$", IGNORE_CASE)
        ).toList()

        if (profiles.size > 1) {
            this.nautilus.logger.warning("Multiple profiles were found for '${username}', requesting a unique id from Mojang!")

            val uniqueId = withContext(Dispatchers.IO) {
                nautilus.server.getPlayerUniqueId(username)
            } ?: return null

            return this.profile(uniqueId)
        }

        return profiles.firstOrNull()
    }

    /**
     * Create a new profile if no profile exists for
     * the target unique id
     *
     * @param uniqueId The unique id to create the profile
     * @param fill Whether to fill in other properties as well, such as the name and skin
     * @return The profile associated with the unique id, which is never null
     */
    suspend fun createProfileIfNonexistent(uniqueId: UUID, fill: Boolean = true): ProfileSnapshot {
        var profile = this.profile(uniqueId)

        if (profile == null) {
            profile = ProfileSnapshot(uniqueId)

            if (fill) {
                val playerProfile = this.nautilus.server.createProfile(uniqueId)

                withContext(Dispatchers.IO) {
                    playerProfile.complete(true)
                }

                val username = playerProfile.name
                val skin = playerProfile.skin()

                if (username != null) profile = profile.copy(lastKnownName = username)
                if (skin != null) profile = profile.copy(lastKnownSkin = skin)
            }

            this.profiles.save(profile)
        } else {
            this.nautilus.scheduler.launch {
                profiles.save(profile)
            }
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

        val event = ProfileUpdateEvent(
            profile = profile,
            previous = previous
        )

        if (Bukkit.isPrimaryThread()) {
            event.callEvent()
            return
        }

        this.nautilus.server.scheduler.runTask(this.nautilus, Runnable {
            event.callEvent()
        })
    }

    /**
     * Cache a profile
     *
     * @param profile The profile to cache
     * @return The previous profile if exists, otherwise null
     */
    private fun cacheProfile(profile: ProfileSnapshot): ProfileSnapshot? {
        nautilus.logger.info("Caching the profile of ${profile.player()?.name ?: profile.lastKnownName} (${profile.uniqueId})")
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
                    val profile = createProfileIfNonexistent(event.uniqueId, fill = false)
                    cacheProfile(profile)
                    ProfileLoadEvent(profile).callEvent()
                }
            }
        }

        @EventHandler
        fun on(event: PlayerConnectionCloseEvent) {
            synchronized(this) {
                profileIfCached(event.playerUniqueId)?.let { ProfileUnloadEvent(it).callEvent() }
                nautilus.logger.info("Removing the cached profile of ${event.playerName} (${event.playerUniqueId})")
                profileCache -= event.playerUniqueId
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        fun on(event: PlayerLoginEvent) {
            val player = event.player
            val profile = player.profile()
            val playerProfile = player.playerProfile
            val skin = playerProfile.skin()

            nautilus.scheduler.launch {
                profile.update(
                    setValue(ProfileSnapshot::lastKnownName, player.name),
                    setValue(ProfileSnapshot::lastOnline, LocalDateTime.now()),
                    setValue(ProfileSnapshot::lastKnownSkin, skin)
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

        @EventHandler
        fun on(event: PlayerInteractEntityEvent) {
            val player = event.player
            val target = event.rightClicked as? Player ?: return
            val targetProfile = target.profile()

            if (event.hand != EquipmentSlot.HAND) return

            // Don't block shields from activating when accidentally clicking a player
            if (player.inventory.itemInMainHand.type == Material.SHIELD
                || player.inventory.itemInOffHand.type == Material.SHIELD) return

            if (targetProfile.maskName != null) {
                player.showTitle(title(
                    empty(),
                    text().color(color(255,105,97)).decorate(ITALIC)
                        .append(targetProfile.nameComponent(showPrefix = false, showSuffix = false, showHover = false, isClickable = false))
                        .appendSpace().append(text("has requested to hide their profile"))
                        .build(),
                    times(Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofMillis(400))
                ))
                player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 10f, 2f)
                return
            }

            ProfileInterface(targetProfile, playSound = true).open(player)
            event.isCancelled = true
        }
    }
}