package net.tjalp.nautilus.event

import net.tjalp.nautilus.clan.ClanSnapshot
import org.bukkit.event.Event

/**
 * Represents a clan related event
 */
abstract class ClanEvent(
    val clan: ClanSnapshot
): Event()