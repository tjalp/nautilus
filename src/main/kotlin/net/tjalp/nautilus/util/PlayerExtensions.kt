package net.tjalp.nautilus.util

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.profile.ProfileManager
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import org.bukkit.entity.Player

/**
 * @see [ProfileManager.profile]
 */
fun Player.profile(): ProfileSnapshot = Nautilus.get().profiles.profile(this)

/**
 * @see [ProfileSnapshot.has]
 */
infix fun Player.has(permission: String): Boolean = this.profile().has(permission)

/**
 * Refresh a player's skin by resending their data.
 */
fun Player.refresh() {
    this.playerProfile = this.playerProfile
}