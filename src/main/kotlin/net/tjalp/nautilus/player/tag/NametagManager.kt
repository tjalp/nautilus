package net.tjalp.nautilus.player.tag

import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.event.Subscribe
import me.neznamy.tab.api.event.player.PlayerLoadEvent
import me.neznamy.tab.api.team.UnlimitedNametagManager
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.primaryRank
import net.tjalp.nautilus.util.profile
import net.tjalp.nautilus.util.register
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * The nametag manager manages everything to do
 * with player list names, name tags etc.
 */
class NametagManager(
    val nautilus: Nautilus
) {

    private val tabApi = TabAPI.getInstance()
    private val teamManager = tabApi.teamManager
    private val serializer = LegacyComponentSerializer.legacyAmpersand()

    init {
        val listener = NametagListener()

        listener.register()
        this.tabApi.eventBus.register(listener)
    }

    /**
     * Update a player's name tag and tab name
     *
     * @param profile The profile to update from
     */
    fun update(profile: ProfileSnapshot) {
        val tabPlayer = tabApi.getPlayer(profile.uniqueId) ?: return
        val rank = profile.primaryRank()

        if (!tabPlayer.isLoaded) return

        if (rank.prefix.content().isNotEmpty()) this.teamManager.setPrefix(tabPlayer, serializer.serialize(rank.prefix) + " ")
        if (rank.suffix.content().isNotEmpty()) this.teamManager.setSuffix(tabPlayer, " " + serializer.serialize(rank.suffix))

        if (teamManager is UnlimitedNametagManager) {
            teamManager.setName(
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

        @EventHandler
        fun on(event: ProfileUpdateEvent) {
            val player = event.player
            val profile = event.profile
            val prev = event.previous
            val component = profile.nameComponent(showSuffix = false)

            if (profile.permissionInfo.ranks != prev?.permissionInfo?.ranks) {
                update(profile)

                player?.displayName(component)
                player?.playerListName(component)
            }
        }

        @EventHandler
        fun on(event: PlayerJoinEvent) {
            val player = event.player
            val component = player.profile().nameComponent(showSuffix = false)

            player.displayName(component)
            player.playerListName(component)
        }
    }
}