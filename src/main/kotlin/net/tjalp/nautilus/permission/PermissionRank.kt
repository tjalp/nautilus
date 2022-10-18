package net.tjalp.nautilus.permission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.tjalp.nautilus.Nautilus

/**
 * A single permission rank that has permissions,
 * a prefix, suffix, name formatting, chat color etc.
 */
class PermissionRank(
    val id: String,
    val weight: Int,
    val name: String,
    val nameColor: TextColor,
    val prefix: TextComponent = Component.empty(),
    val suffix: TextComponent = Component.empty(),
    val chatColor: TextColor? = null,
    val permissions: Set<String> = emptySet(),
    inherits: Set<String> = emptySet()
) {

    val inherits: Set<PermissionRank> = inherits.map {
        Nautilus.get().perms.getRank(it)
    }.toHashSet()
}