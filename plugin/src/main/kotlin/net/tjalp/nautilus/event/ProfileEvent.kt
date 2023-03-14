package net.tjalp.nautilus.event

import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.player
import org.bukkit.entity.Player
import org.bukkit.event.Event

/**
 * Represents a profile related event
 */
abstract class ProfileEvent(
    val profile: ProfileSnapshot,
    async: Boolean = false
) : Event(async) {

    /**
     * Get the **online** player of this event.
     * Will throw an exception if not online.
     */
    val onlinePlayer: Player
        get() = this.profile.player()!!

    /**
     * Get the player of this event.
     * May be null of no player was found.
     */
    val player = this.profile.player()
}