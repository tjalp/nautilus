package net.tjalp.aquarium.registry

import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.permissions.PermissionDefault.OP

val DECORATED_CHAT = perm("decorated_chat", OP)

private fun perm(perm: String, default: PermissionDefault? = null): Permission {
    return Permission("aquarium.$perm", default)
}