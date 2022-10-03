package net.tjalp.nautilus.registry

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.permission.PermissionRank
import net.tjalp.nautilus.util.mini

// List of ranks
const val DEFAULT_RANK = "default"
const val MEDIA_RANK = "media"
const val ADMIN_RANK = "admin"

/**
 * Register all ranks
 */
fun registerRanks(nautilus: Nautilus) {
    val perms = nautilus.perms

    perms.registerRank(PermissionRank(
        id = DEFAULT_RANK,
        name = "Default",
        displayName = mini("<#80ddb1>Default"),
        prefix = mini("<white>ꐁ"),
        chatFormat = mini("<#99dbbb>")
    ))

    perms.registerRank(PermissionRank(
        id = MEDIA_RANK,
        name = "Media",
        displayName = mini("<#f09990>Media"),
        prefix = mini("<white>ꐂ"),
        chatFormat = mini("<#efada7>"),
        inherits = setOf(
            DEFAULT_RANK
        )
    ))

    perms.registerRank(PermissionRank(
        id = ADMIN_RANK,
        name = "Admin",
        displayName = mini("<#c3b1e1>Admin"),
        prefix = mini("<white>ꐀ"),
        chatFormat = mini("<#dac7fc>"),
        inherits = setOf(
            DEFAULT_RANK,
            MEDIA_RANK
        )
    ))
}