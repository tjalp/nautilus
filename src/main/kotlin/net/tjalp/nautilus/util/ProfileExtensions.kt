package net.tjalp.nautilus.util

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.permission.PermissionRank
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import org.bukkit.entity.Player

/**
 * Get an online [Player] from a profile
 *
 * @return A [Player] if an online player was found, otherwise null
 */
fun ProfileSnapshot.player(): Player? = Nautilus.get().server.getPlayer(this.uniqueId)

/**
 * Get a set of [PermissionRank]s of a profile
 *
 * @return A set of [PermissionRank]s
 */
fun ProfileSnapshot.ranks(): Set<PermissionRank> {
    val hashSet = HashSet<PermissionRank>()
    val profileRanks = this.permissionInfo.ranks

    for (rank in Nautilus.get().perms.ranks) {
        if (rank.id in profileRanks) hashSet += rank
    }

    return hashSet
}