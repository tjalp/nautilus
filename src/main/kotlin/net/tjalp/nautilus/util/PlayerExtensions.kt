package net.tjalp.nautilus.util

import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.sound.Sound.Source.MASTER
import net.kyori.adventure.text.Component
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.Players
import net.tjalp.nautilus.player.profile.ProfileManager
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import org.bukkit.Sound.UI_BUTTON_CLICK
import org.bukkit.entity.Player

/**
 * @see [ProfileManager.profile]
 */
fun Player.profile(): ProfileSnapshot = Nautilus.get().profiles.profile(this)

/**
 * @see [ProfileSnapshot.has]
 */
infix fun Player.has(permission: String): Boolean = this.hasPermission(permission) || this.profile().has(permission)

/**
 * Refresh a player's skin by resending their data.
 */
fun Player.refresh() {
    this.playerProfile = this.playerProfile
}

/**
 * @see [Players.actionBar]
 */
fun Player.actionBar(component: Component?) {
    Players.actionBar(this, component)
}

/**
 * @see [Players.actionBar]
 */
fun Player.actionBar() = Players.actionBar(this)

/**
 * Play a simple click sound for the player
 */
fun Player.playClickSound() {
    this.playSound(sound(UI_BUTTON_CLICK.key(), MASTER, 1f, 1f))
}