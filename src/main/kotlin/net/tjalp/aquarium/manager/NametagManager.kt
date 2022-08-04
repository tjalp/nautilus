package net.tjalp.aquarium.manager

import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.team.UnlimitedNametagManager
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.util.getPrefix
import net.tjalp.aquarium.util.getSuffix
import org.bukkit.entity.Player

/**
 * Manages all the name tags and makes sure everything is organized
 */
class NametagManager(
    val aquarium: Aquarium,
    val tabApi: TabAPI
) {

    /**
     * Update a player's name tag
     *
     * @param player The player to update the name tag of
     * @param tag The value to update the tag to
     */
    fun update(player: Player) {
        val teamManager = tabApi.teamManager
        val tabPlayer = tabApi.getPlayer(player.uniqueId) ?: return

        if (!tabPlayer.isLoaded) return

        val serializer = LegacyComponentSerializer.legacyAmpersand()
        val mini = MiniMessage.miniMessage()
        val prefix = serializer.serialize(mini.deserialize(player.getPrefix() ?: ""))
        val suffix = serializer.serialize(mini.deserialize(player.getSuffix() ?: ""))

        teamManager.setPrefix(tabPlayer, prefix)
        teamManager.setSuffix(tabPlayer, suffix)

        if (teamManager is UnlimitedNametagManager) {
            teamManager.setName(tabPlayer, serializer.serialize(
                MiniMessage.miniMessage().deserialize("<rainbow>${player.name}</rainbow>")
            ))
        }
    }
}