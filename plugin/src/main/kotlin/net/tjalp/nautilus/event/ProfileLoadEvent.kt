package net.tjalp.nautilus.event

import net.tjalp.nautilus.player.profile.ProfileSnapshot
import org.bukkit.event.HandlerList

class ProfileLoadEvent(
    profile: ProfileSnapshot
) : ProfileEvent(profile, true) {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {

        @JvmStatic
        val handlerList = HandlerList()
    }
}