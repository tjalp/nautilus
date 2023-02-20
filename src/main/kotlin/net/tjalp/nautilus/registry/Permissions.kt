package net.tjalp.nautilus.registry

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextColor.fromHexString
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.permission.PermissionRank
import net.tjalp.nautilus.util.mini

// List of ranks
const val DEFAULT_RANK = "default"
const val MEDIA_RANK = "media"
const val ADMIN_RANK = "admin"

// Permissions
const val OPERATOR = "*"
const val DECORATED_CHAT = "nautilus.decorated_chat"
const val DISGUISE_COMMAND = "nautilus.command.disguise"
const val IDENTITY_COMMAND = "nautilus.command.identity"
const val MASK_COMMAND = "nautilus.command.mask"
const val PROFILE_COMMAND = "nautilus.command.profile"
const val REAL_NAME_COMMAND = "nautilus.command.real_name"
const val NAUTILUS_ENCHANTMENT_COMMAND = "nautilus.command.nautilus_enchantment"
const val NAUTILUS_ITEM_COMMAND = "nautilus.command.nautilus_item"
const val VIEW_REAL_NAMES = "nautilus.mask.view_real_names"

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
        chatColor = color(153, 219, 187)
    ))

    perms.registerRank(PermissionRank(
        id = MEDIA_RANK,
        weight = 1,
        name = "Media",
        nameColor = fromHexString("#f09990")!!,
        prefix = mini("<white>ꐂ") as TextComponent,
        chatColor = color(239, 173, 167),
        inherits = setOf(
            DEFAULT_RANK
        ),
        permissions = setOf(
            DECORATED_CHAT,
            DISGUISE_COMMAND,
            MASK_COMMAND
        )
    ))

    perms.registerRank(PermissionRank(
        id = ADMIN_RANK,
        weight = 2,
        name = "Administrator",
        nameColor = fromHexString("#c3b1e1")!!,
        prefix = mini("<white>ꐀ") as TextComponent,
        chatColor = color(218, 199, 252),
        inherits = setOf(
            DEFAULT_RANK
        ),
        permissions = setOf(
            OPERATOR
        )
    ))
}