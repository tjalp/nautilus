package net.tjalp.nautilus.player.profile

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.register
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.*

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
            // todo actually get the profile
            synchronized(this) {
                nautilus.logger.warning("Caching the profile of ${event.name} (${event.uniqueId})")
                cacheProfile(ProfileSnapshot(event.uniqueId))
            }
        }

        @EventHandler
        fun on(event: PlayerConnectionCloseEvent) {
            synchronized(this) {
                nautilus.logger.warning("Removing the cached profile of ${event.playerName} (${event.playerUniqueId})")
                profileCache -= event.playerUniqueId
            }
        }
    }
}