package net.tjalp.nautilus.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent.runCommand
import net.kyori.adventure.text.event.HoverEvent.showText
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
    return Nautilus.get().perms.getRanks(this)
}

/**
 * Get the primary rank of a [ProfileSnapshot]
 *
 * @return The primary [PermissionRank] of the profile
 */
fun ProfileSnapshot.primaryRank(): PermissionRank {
    return Nautilus.get().perms.getPrimaryRank(this)
}

/**
 * Get the display rank of a [ProfileSnapshot]
 *
 * @return The display [PermissionRank] of the profile
 */
fun ProfileSnapshot.displayRank(): PermissionRank {
    return Nautilus.get().masking.rank(this) ?: return primaryRank()
}

/**
 * Get the display name of a [ProfileSnapshot]
 */
fun ProfileSnapshot.displayName(): String {
    val player = this.player()
    val masking = Nautilus.get().masking

    return masking.username(this) ?: (player?.name ?: this.lastKnownName)
}

/**
 * Get a component of a profile
 *
 * @param showPrefix Whether to show the prefix
 * @param showSuffix Whether to show the suffix
 * @return A formatted [Component] of the profile
 */
fun ProfileSnapshot.nameComponent(
    useMask: Boolean = true,
    showPrefix: Boolean = true,
    showSuffix: Boolean = true
): Component {
    val player = this.player()
    val username = if (useMask) text(this.displayName()) else (player?.name() ?: text(this.lastKnownName)) as TextComponent
    val rank = if (useMask) this.displayRank() else this.primaryRank()
    val component = text()

    if (showPrefix && rank.prefix.content().isNotEmpty()) component.append(rank.prefix).append(space())
    component.append(username.color(rank.nameColor)) // todo make this better
    if (showSuffix && rank.suffix.content().isNotEmpty()) component.append(space()).append(rank.suffix)

    component.hoverEvent(showText(mini("<rainbow>#savetheturtle (me)")))
    component.clickEvent(runCommand("/profile ${username.content()}"))

    return component.build().compact()
}

/**
 * Returns whether the profile has a permission
 *
 * @param permission The permission to check
 * @return Whether the profile has the permission or not
 */
infix fun ProfileSnapshot.has(permission: String): Boolean {
    return Nautilus.get().perms.has(this, permission)
}