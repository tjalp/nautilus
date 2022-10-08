package net.tjalp.nautilus.registry

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor.fromHexString
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.permission.PermissionRank
import net.tjalp.nautilus.util.mini

// List of ranks
const val DEFAULT_RANK = "default"
const val MEDIA_RANK = "media"
const val ADMIN_RANK = "admin"

// Permissions
const val DECORATED_CHAT = "nautilus.decorated_chat"

/**
 * Register all ranks
 */
fun registerRanks(nautilus: Nautilus) {
    val perms = nautilus.perms

    perms.registerRank(PermissionRank(
        id = DEFAULT_RANK,
        weight = 0,
        name = "Default",
        nameColor = fromHexString("#80ddb1")!!,
        prefix = mini("<white>ꐁ") as TextComponent,
        chatFormat = mini("<#99dbbb>")
    ))

    perms.registerRank(PermissionRank(
        id = MEDIA_RANK,
        weight = 1,
        name = "Media",
        nameColor = fromHexString("#f09990")!!,
        prefix = mini("<white>ꐂ") as TextComponent,
        chatFormat = mini("<#efada7>"),
        inherits = setOf(
            DEFAULT_RANK
        ),
        permissions = setOf(
            DECORATED_CHAT
        )
    ))

    perms.registerRank(PermissionRank(
        id = ADMIN_RANK,
        weight = 2,
        name = "Admin",
        nameColor = fromHexString("#c3b1e1")!!,
        prefix = mini("<white>ꐀ") as TextComponent,
        chatFormat = mini("<#dac7fc>"),
        inherits = setOf(
            DEFAULT_RANK,
            MEDIA_RANK
        )
    ))
}