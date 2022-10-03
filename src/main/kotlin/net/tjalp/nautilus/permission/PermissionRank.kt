package net.tjalp.nautilus.permission

import net.kyori.adventure.text.Component
import net.tjalp.nautilus.Nautilus

/**
 * A single permission rank that has permissions,
 * a prefix, suffix, name formatting, chat color etc.
 */
class PermissionRank(
    val id: String,
    val name: String,
    val displayName: Component,
    val prefix: Component = Component.empty(),
    val suffix: Component = Component.empty(),
    val chatFormat: Component = Component.empty(),
    val permissions: Set<String> = emptySet(),
    inherits: Set<String> = emptySet()
) {

    val inherits: Set<PermissionRank> = inherits.map {
        Nautilus.get().perms.getRank(it)
    }.toHashSet()
}