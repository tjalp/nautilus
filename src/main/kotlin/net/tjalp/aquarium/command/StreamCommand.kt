package net.tjalp.aquarium.command

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.processing.CommandContainer
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandContainer
class StreamCommand {

    @CommandMethod("stream start")
    fun start(player: Player) {
        player.sendMessage(text("Starting stream!"))
    }

    @CommandMethod("stream end")
    fun end(player: Player) {
        player.sendMessage(text("Ending stream!"))
    }
}