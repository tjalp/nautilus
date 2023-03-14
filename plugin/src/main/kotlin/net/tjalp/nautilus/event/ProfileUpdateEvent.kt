package net.tjalp.nautilus.event

import net.tjalp.nautilus.player.profile.ProfileSnapshot
import org.bukkit.event.HandlerList

class ProfileUpdateEvent(
    profile: ProfileSnapshot,
    val previous: ProfileSnapshot?
) : ProfileEvent(profile) {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {

        @JvmStatic
        val handlerList = HandlerList()
    }
}