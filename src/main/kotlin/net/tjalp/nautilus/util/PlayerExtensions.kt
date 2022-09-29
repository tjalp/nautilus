package net.tjalp.nautilus.util

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.profile.ProfileManager
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import org.bukkit.entity.Player

/**
 * @see [ProfileManager.profile]
 */
fun Player.profile(): ProfileSnapshot = Nautilus.get().profiles.profile(this)