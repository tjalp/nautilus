package net.tjalp.nautilus.util

import com.destroystokyo.paper.profile.ProfileProperty
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
 * Note that this does not apply the skin to the player
 * itself, otherwise chat messages get broken.
 */
fun Player.setSkin(skin: SkinBlob?) {
//    val nautilus = Nautilus.get()
//
////    nautilus.server.onlinePlayers.stream()
////        .filter { online -> online != this }
////        .filter { online -> online.canSee(this) }
////        .forEach {
////            it.hidePlayer(nautilus, this)
////            it.showPlayer(nautilus, this)
////        }
    val blob = skin ?: profile().lastKnownSkin
    val newProfile = this.playerProfile.apply {
        setProperty(ProfileProperty("textures", blob.value, blob.signature))
    }

    this.playerProfile = newProfile
}