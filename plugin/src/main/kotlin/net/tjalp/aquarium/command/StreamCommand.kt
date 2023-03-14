package net.tjalp.aquarium.command

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.processing.CommandContainer
import net.kyori.adventure.text.Component.text
import net.tjalp.aquarium.registry.COMMAND_STREAM
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandContainer
@CommandPermission(COMMAND_STREAM)
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