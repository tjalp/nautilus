package net.tjalp.nautilus.player.disguise

import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.Disguise
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MiscDisguise
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.player
import net.tjalp.nautilus.util.register
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.litote.kmongo.setValue
import kotlin.system.measureTimeMillis

/**
 * Manages everything that has to do with a player's appearance.
 * Not to be confused with masking, which makes a player 'anonymous'.
 */
class DisguiseManager(private val nautilus: Nautilus) {

    init {
        DisguiseListener().register()
    }

    /**
     * Set a player's disguise to an entity type.
     *
     * @param profile The profile to disguise
     * @param entity The entity type to disguise to
     */
    suspend fun disguise(profile: ProfileSnapshot, entity: EntityType) {
        val time = measureTimeMillis {
            profile.update(
                setValue(ProfileSnapshot::disguise, entity.key.toString())
            )
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

        @EventHandler
        fun on(event: ProfileUpdateEvent) {
            val profile = event.profile
            val prev = event.previous ?: return
            val disguise = disguise(profile)

            if (disguise != disguise(prev)) {
                if (disguise == null) {
                    DisguiseAPI.undisguiseToAll(event.onlinePlayer)
                    return
                }

                val disguiseType = DisguiseType.getType(disguise)
                val libDisguise: Disguise = if (disguise == EntityType.PLAYER) PlayerDisguise(nautilus.masking.generateGameProfile(profile))
                else if (disguise.isAlive) MobDisguise(disguiseType)
                else MiscDisguise(disguiseType)

                DisguiseAPI.disguiseToAll(event.onlinePlayer, libDisguise)
            }
        }
    }
}