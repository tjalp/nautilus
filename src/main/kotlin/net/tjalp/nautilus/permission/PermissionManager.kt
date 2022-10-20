package net.tjalp.nautilus.permission

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.player
import net.tjalp.nautilus.util.register
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * The permission manager contains information about
 * all the registered ranks, permissions etc.
 */
class PermissionManager(val nautilus: Nautilus) {

    private val registeredRanks = mutableSetOf<PermissionRank>()
    private val ranksById = HashMap<String, PermissionRank>()
    private var default: PermissionRank? = null

    init {
        // Register the listener
        PermissionListener().register()
    }

    /**
     * A list of all registered ranks
     */
    val ranks: List<PermissionRank>
        get() = ArrayList(this.registeredRanks)

    /**
     * The default rank that everyone has
     */
    val defaultRank: PermissionRank
        get() = this.default ?: throw IllegalStateException("Default rank is not registered")

    /**
     * Register a new [PermissionRank]
     */
    fun registerRank(rank: PermissionRank) {
        check(rank.id !in this.ranksById) { "Rank already registered" }

        this.registeredRanks += rank
        this.ranksById[rank.id] = rank

        if (this.default == null) this.default = rank
    }

    /**
     * Get a rank from an id
     *
     * @param id The id of the rank to get
     * @return The [PermissionRank] associated with this id
     */
    fun getRank(id: String): PermissionRank {
        return this.ranksById[id.lowercase()] ?: throw IllegalArgumentException("Rank by id $id does not exist!")
    }

    /**
     * Returns whether a rank with the given id exists
     *
     * @param id The id to check
     * @return true if exists, otherwise false
     */
    fun rankExists(id: String): Boolean {
        return id in this.ranksById
    }

    /**
     * Gets a list of ranks of a [ProfileSnapshot]
     */
    fun getRanks(profile: ProfileSnapshot): Set<PermissionRank> {
        val hashSet = HashSet<PermissionRank>()
        val profileRanks = profile.permissionInfo.ranks

        for (rank in ranks) {
            if (rank.id in profileRanks) hashSet += rank
        }

        return hashSet
    }

    /**
     * Returns whether a rank (or an inheritant) has
     * a certain permission
     *
     * @param rank The rank to check
     * @param permission The permission to check
     * @return true if it has the permission, otherwise false
     */
    fun has(rank: PermissionRank, permission: String): Boolean {
        if (permission in rank.permissions) return true

        for (inheritant in rank.inherits) {
            if (permission in inheritant.permissions) return true
        }

        return false
    }

    /**
     * Returns whether a profile has a certain
     * permission
     *
     * @param profile The profile to check
     * @param permission The permission to check
     * @return true if it has the permission, otherwise false
     */
    fun has(profile: ProfileSnapshot, permission: String): Boolean {
        if (profile.player()?.isOp == true) return true

        val permissionInfo = profile.permissionInfo

        if (permission in permissionInfo.permissions) return true

        for (rank in getRanks(profile)) {
            if (has(rank, permission)) return true
        }

        return false
    }

    /**
     * Get the primary rank of a profile
     *
     * @param profile The profile to use
     * @return The primary rank of this profile
     */
    fun getPrimaryRank(profile: ProfileSnapshot): PermissionRank {
        return getRanks(profile).maxByOrNull { it.weight } ?: this.defaultRank
    }

    /**
     * The permission listener to apply permissions
     */
    private inner class PermissionListener : Listener {

        @EventHandler(priority = EventPriority.LOW)
        fun on(event: PlayerJoinEvent) {
            val player = event.player
            val globalPermissions = nautilus.server.pluginManager.permissions

            if (player.isOp) return

            for (perm in globalPermissions) player.addAttachment(nautilus, perm.name, false)

            player.updateCommands()
        }
    }
}