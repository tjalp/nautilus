package net.tjalp.nautilus.player.profile.data

import net.tjalp.nautilus.Nautilus

/**
 * Data class that contains permission info
 * about a profile.
 */
class PermissionInfo(
    val ranks: Set<String> = setOf(Nautilus.get().perms.defaultRank.id),
    val permissions: Set<String> = emptySet()
)