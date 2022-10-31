package net.tjalp.nautilus.player.disguise

import me.libraryaddict.disguise.DisguiseAPI.disguiseToAll
import me.libraryaddict.disguise.DisguiseAPI.undisguiseToAll
import me.libraryaddict.disguise.LibsDisguises
import me.libraryaddict.disguise.disguisetypes.*
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.player
import net.tjalp.nautilus.util.profile
import net.tjalp.nautilus.util.register
import org.bukkit.command.PluginCommand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.litote.kmongo.setValue
import kotlin.system.measureTimeMillis

/**
 * Manages everything that has to do with a player's appearance.
 * Not to be confused with masking, which makes a player 'anonymous'.
 */
class DisguiseManager(private val nautilus: Nautilus) {

    init {
        val server = this.nautilus.server

        DisguiseListener().register()

        (server.pluginManager.getPlugin("LibsDisguises") as LibsDisguises).unregisterCommands(true)
    }

    /**
     * Set a player's disguise to an entity type.
     *
     * @param profile The profile to disguise
     * @param entity The entity type to disguise to
     */
    suspend fun disguise(profile: ProfileSnapshot, entity: EntityType?) {
        val time = measureTimeMillis {
            profile.update(
                setValue(ProfileSnapshot::disguise, entity?.key?.toString())
            )
        }

        if (entity == null) {
            profile.player()?.sendMessage(text("Your disguise has been cleared", GRAY)
                .append(text(" (${time}ms)", WHITE)))
            return
        }

        profile.player()?.sendMessage(text().color(GRAY)
            .append(text("Your disguise has been set to "))
            .append(translatable(entity.translationKey(), WHITE))
            .append(text(" (${time}ms)"))
            .build())
    }

    fun disguise(profile: ProfileSnapshot): EntityType? {
        val disguise = profile.disguise ?: return null

        return EntityType.values().firstOrNull { it.key.toString() == disguise }
    }

    /**
     * The disguise manager listener which listens
     * for disguise changes
     */
    private inner class DisguiseListener : Listener {

        private fun updateDisguise(player: Player, entityType: EntityType?) {
            if (entityType == null) {
                undisguiseToAll(player)
                return
            }

            val disguiseType = DisguiseType.getType(entityType)
            val libDisguise: Disguise = if (entityType == EntityType.PLAYER) PlayerDisguise(nautilus.masking.generateGameProfile(player.profile()))
            else if (entityType.isAlive) MobDisguise(disguiseType)
            else MiscDisguise(disguiseType)

            disguiseToAll(player, libDisguise)
        }

        @EventHandler
        fun on(event: PlayerJoinEvent) {
            val player = event.player
            val profile = player.profile()
            val disguise = disguise(profile)

            if (disguise != null) updateDisguise(player, disguise)
        }

        @EventHandler
        fun on(event: ProfileUpdateEvent) {
            val profile = event.profile
            val prev = event.previous ?: return
            val disguise = disguise(profile)

            if (disguise != disguise(prev)) updateDisguise(event.onlinePlayer, disguise)
        }
    }
}