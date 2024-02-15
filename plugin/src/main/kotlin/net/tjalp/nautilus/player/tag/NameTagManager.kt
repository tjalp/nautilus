package net.tjalp.nautilus.player.tag

import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.event.Subscribe
import me.neznamy.tab.api.event.player.PlayerLoadEvent
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * The nametag manager manages everything to do
 * with player list names, name tags etc.
 */
class NameTagManager(
    private val nautilus: Nautilus
) {

    private val tabApi = TabAPI.getInstance()
    private val nameTagManager = this.tabApi.nameTagManager
    private val serializer = LegacyComponentSerializer.legacyAmpersand()
    private val masking = this.nautilus.masking

    init {
        val listener = NametagListener()

        listener.register()
        this.tabApi.eventBus?.register(listener)
    }

    /**
     * Update a player's name tag and tab name
     *
     * @param profile The profile to update from
     */
    fun update(profile: ProfileSnapshot) {
        val player = profile.player()
        val component = profile.nameComponent(showSuffix = false)

        player?.displayName(component)
        player?.playerListName(component)

        val tabPlayer = tabApi.getPlayer(profile.uniqueId) ?: return
        val rank = profile.displayRank()
        val index = nautilus.perms.ranks.sortedByDescending { it.weight }.indexOf(rank)

        if (!tabPlayer.isLoaded) return

        if (rank.prefix.content().isNotEmpty()) this.nameTagManager?.setPrefix(tabPlayer, serializer.serialize(rank.prefix) + " ")
        if (rank.suffix.content().isNotEmpty()) this.nameTagManager?.setSuffix(tabPlayer, " " + serializer.serialize(rank.suffix))
        if (index >= 0) this.tabApi.sortingManager?.forceTeamName(tabPlayer, "$index-${rank.id}".take(16))

        if (nameTagManager is UnlimitedNameTagManager) {
            nameTagManager.setName(
                tabPlayer,
                serializer.serialize(profile.nameComponent(showPrefix = false, showSuffix = false))
            )
        }
    }

    /**
     * The class to manage whenever anything changes
     */
    @Suppress("UNUSED")
    inner class NametagListener : Listener {

        @Subscribe
        fun on(event: PlayerLoadEvent) {
            val player = event.player
            val bukkitPlayer = player.player as Player

            update(bukkitPlayer.profile())
        }

        @EventHandler(priority = EventPriority.LOW)
        fun on(event: ProfileUpdateEvent) {
            val profile = event.profile
            val prev = event.previous

            if (profile.permissionInfo.ranks != prev?.permissionInfo?.ranks
                || masking.username(profile) != masking.username(prev)
                || masking.rank(profile) != masking.rank(prev)
            ) {
                update(profile)
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        fun on(event: PlayerJoinEvent) {
            update(event.player.profile())
        }
    }
}