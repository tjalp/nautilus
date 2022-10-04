package net.tjalp.nautilus.player.profile.data

import net.tjalp.nautilus.Nautilus

/**
 * Data class that contains permission info
 * about a profile.
 */
class PermissionInfo(
    var ranks: Set<String> = setOf(Nautilus.get().perms.defaultRank.id),
    var permissions: Set<String> = emptySet()
)