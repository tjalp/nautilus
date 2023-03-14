package net.tjalp.nautilus.event

import net.tjalp.nautilus.clan.ClanSnapshot
import org.bukkit.event.HandlerList

class ClanUpdateEvent(
    clan: ClanSnapshot,
    val previous: ClanSnapshot?
) : ClanEvent(clan) {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {

        @JvmStatic
        val handlerList = HandlerList()
    }
}