package net.tjalp.aquarium.command

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.processing.CommandContainer
import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.util.mini
import org.bukkit.entity.Player
import java.util.*

@Suppress("UNUSED")
@CommandContainer
class DisguiseCommand {

    val lp = Aquarium.luckperms

    @CommandMethod("disguise rank <rank>")
    fun rank(
        player: Player,
        @Argument("rank") rankArg: String
    ) {
        val group = this.lp.groupManager.getGroup(rankArg.lowercase())

        if (group == null) {
            player.sendMessage(mini("<red>That rank does not exist or isn't loaded!"))
            return
        }

        disguises[player.uniqueId] = rankArg.lowercase()

        lp.userManager.modifyUser(player.uniqueId) {}
    }

    @CommandMethod("disguise player <player>")
    fun player(
        player: Player,
        @Argument("player") playerArg: String
    ) {
        
    }

    companion object {
        val disguises = hashMapOf<UUID, String>()
    }
}