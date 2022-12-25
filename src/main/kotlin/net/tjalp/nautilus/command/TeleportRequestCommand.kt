package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.teleport.PlayerTeleportRequest
import net.tjalp.nautilus.registry.DISPLAY_NAME_SUGGESTIONS
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.profile
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TeleportRequestCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    init {
        val builder = builder("teleportrequest", "tprequest", "requestteleport", "requesttp", "tpa", "tpask", "teleportask")
            .senderType(Player::class.java)
        val targetArg = StringArgument.builder<CommandSender>("target")
            .quoted()
            .withSuggestionsProvider(DISPLAY_NAME_SUGGESTIONS)
            .build()

        register(builder.literal("send").argument(targetArg.copy()).handler {
            this.send(it.sender as Player, it.get(targetArg))
        })

        register(builder.literal("accept").argument(targetArg.copy()).handler {
            this.accept(it.sender as Player, it.get(targetArg))
        })

        register(builder.literal("deny").argument(targetArg.copy()).handler {
            this.deny(it.sender as Player, it.get(targetArg))
        })

        register(builder.literal("cancel").argument(targetArg.copy()).handler {
            this.cancel(it.sender as Player, it.get(targetArg))
        })
    }

    private fun send(sender: Player, targetArg: String) {
        val target = this.nautilus.masking.playerFromDisplayName(targetArg)

        if (target == null) {
            sender.sendMessage(text("No targets found", RED))
            return
        }

        PlayerTeleportRequest(sender, target).request()
    }

    private fun accept(sender: Player, targetArg: String) {
        handleTeleportRequest(sender, targetArg)?.accept()
    }

    private fun deny(sender: Player, targetArg: String) {
        handleTeleportRequest(sender, targetArg)?.deny()
    }

    private fun cancel(sender: Player, targetArg: String) {
        val target = this.nautilus.masking.playerFromDisplayName(targetArg)

        if (target == null) {
            sender.sendMessage(text("No targets found", RED))
            return
        }

        val request = PlayerTeleportRequest.requests().firstOrNull { it.source == sender && it.target == target }

        if (request == null) {
            sender.sendMessage(text("You have no active teleport request to", RED).appendSpace()
                .append(target.profile().nameComponent(showPrefix = false, showSuffix = false)))
            return
        }

        request.cancel()
    }

    private fun handleTeleportRequest(source: Player, targetArg: String): PlayerTeleportRequest? {
        val target = this.nautilus.masking.playerFromDisplayName(targetArg)

        if (target == null) {
            source.sendMessage(text("No targets found", RED))
            return null
        }

        val request = PlayerTeleportRequest.requests().firstOrNull { it.target == source && it.source == target }

        if (request == null) {
            source.sendMessage(text("You have no active teleport request from", RED).appendSpace()
                .append(target.profile().nameComponent(showPrefix = false, showSuffix = false)))
        }

        return request
    }
}