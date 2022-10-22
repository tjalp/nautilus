package net.tjalp.nautilus.permission

import kotlinx.coroutines.launch
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.registry.OPERATOR
import net.tjalp.nautilus.util.has
import net.tjalp.nautilus.util.permissions
import net.tjalp.nautilus.util.profile
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissibleBase
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import org.litote.kmongo.setValue

/**
 * PermissibleBase for Nautilus.
 *
 * This class overrides all methods from [PermissibleBase] to
 * use Nautilus' implementation instead.
 *
 * All permission checks will be called to Nautilus, instead of
 * Bukkit's implementation. The behavior will stay the same.
 */
@Deprecated("Not finished yet")
class NautilusPermissible(private val nautilus: Nautilus, private val player: Player) : PermissibleBase(player) {

    private val profile
        get() = this.player.profile()

    override fun isPermissionSet(name: String): Boolean = this.profile.permissions().contains(name)
    override fun isPermissionSet(perm: Permission): Boolean = isPermissionSet(perm.name)

    override fun hasPermission(perm: String): Boolean = this.profile has perm
    override fun hasPermission(perm: Permission): Boolean = hasPermission(perm.name)

    override fun setOp(value: Boolean) {}
    override fun isOp(): Boolean = this.profile has OPERATOR

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
        return this.profile.permissions()
            .map { permission ->
                PermissionAttachmentInfo(this, permission, null, true) // TODO Add negative permissions
            }
            .toMutableSet()
    }

    override fun recalculatePermissions() {}
    override fun clearPermissions() {
        val profile = this.profile

        this.nautilus.scheduler.launch {
            profile.update(
                setValue(profile.permissionInfo::permissions, emptySet())
            )
        }
    }

    override fun addAttachment(plugin: Plugin): PermissionAttachment {
        return super.addAttachment(plugin)
    }
}