package net.tjalp.nautilus.player

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor.*
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.util.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * A general utility class for players
 */
object Players {

    private lateinit var nautilus: Nautilus
    private val actionBars = mutableMapOf<Player, Component>()

    /**
     * Initialize the Players object
     */
    fun initialize(nautilus: Nautilus) {
        this.nautilus = nautilus

        PlayersListener().register()
    }

    /**
     * Set the action bar of a player. The action bar
     * will be shown forever until it is set to
     * null or the player has left the server.
     *
     * @param player The player to set the action bar of
     * @param component The component to set the action bar to
     */
    fun actionBar(player: Player, component: Component?) {
        if (component == null) {
            this.actionBars.remove(player)
            player.sendActionBar(empty())
            return
        }

        this.actionBars[player] = component
        player.sendActionBar(component)
    }

    /**
     * Get the action bar of a player.
     */
    fun actionBar(player: Player): Component? {
        return this.actionBars[player]
    }

    private class PlayersListener : Listener {

        private val tasks = mutableMapOf<Player, Int>()

        private fun updateActionBar(player: Player) {
            val profile = player.profile()
            val masking = nautilus.masking
            val disguises = nautilus.disguises
            val joiner = ListJoiner()

            if (masking.username(profile) != null) joiner.add("Username")
            if (masking.rank(profile) != null) joiner.add("Rank")
            if (masking.skin(profile) != null) joiner.add("Skin")
            if (disguises?.disguise(profile) != null) joiner.add("Disguise")

            if (joiner.size() > 0) {
                actionBar(player, text("Visibility Modifiers: ", GRAY)
                    .append(text(joiner.toString(), GOLD)))
            } else actionBar(player, null)
        }

        @EventHandler
        fun on(event: PlayerJoinEvent) {
            val player = event.player

            updateActionBar(player)

            tasks[event.player] = nautilus.server.scheduler.scheduleSyncRepeatingTask(nautilus, {
                val actionBar = actionBar(player) ?: return@scheduleSyncRepeatingTask

                player.sendActionBar(actionBar)
            }, 20, 20)
        }

        @EventHandler
        fun on(event: PlayerQuitEvent) {
            val player = event.player
            val runnable = tasks[player] ?: return

            nautilus.server.scheduler.cancelTask(runnable)
        }

        @EventHandler
        fun on(event: ProfileUpdateEvent) {
            val masking = nautilus.masking
            val disguises = nautilus.disguises
            val profile = event.profile
            val prev = event.previous ?: return

            if (masking.username(profile) != masking.username(prev)
                || masking.skin(profile) != masking.skin(prev)
                || masking.rank(profile) != masking.rank(prev)
                || disguises?.disguise(profile) != disguises?.disguise(prev)
            ) {
                updateActionBar(event.onlinePlayer)
            }
        }

        @EventHandler
        fun on(event: PaperServerListPingEvent) {
            val players = event.playerSample
            val online = nautilus.server.onlinePlayers

            event.numPlayers = online.size
            event.maxPlayers = event.numPlayers + 1
            event.protocolVersion = event.client.protocolVersion
            event.version = "Nautilus"

            players.clear()

            for (player in online) {
                val profile = player.profile()

                players.add(nautilus.server.createProfileExact(player.uniqueId, profile.displayName()))
            }

            val firstMotd = mini("<gradient:#12c2e9:#c471ed:#f7797d><b>Nautilus</b></gradient> <dark_gray>- <gold><b>v${nautilus.description.version}")
            val secondMotd = text("In development | Check back soon!", RED)
            val motd = text()
                .append(centerComponent(firstMotd, MAX_MOTD_WIDTH)).append(newline())
                .append(centerComponent(secondMotd, MAX_MOTD_WIDTH))
                .build()

            event.motd(motd)
        }
    }
}