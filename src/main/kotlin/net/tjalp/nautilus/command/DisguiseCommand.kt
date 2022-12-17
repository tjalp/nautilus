package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.EnumArgument
import kotlinx.coroutines.launch
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.registry.DISGUISE_COMMAND
import net.tjalp.nautilus.util.has
import net.tjalp.nautilus.util.profile
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class DisguiseCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val disguises = this.nautilus.disguises
    private val scheduler = this.nautilus.scheduler

    init {
        val builder = builder("disguise", "dis")
            .senderType(Player::class.java)
            .permission { sender -> if (sender is Player) sender has DISGUISE_COMMAND else true }
        val unbuilder = builder("undisguise", "undis").senderType(Player::class.java)
        val entityTypeArg = EnumArgument.of<CommandSender, EntityType>(EntityType::class.java, "entity")

        val undisguiseCommand = builder.literal("none", "reset", "clear").handler {
            this.nautilus.scheduler.launch { undisguise(it.sender as Player) }
        }

        register(undisguiseCommand)

        register(
            builder.argument(entityTypeArg).handler {
                this.nautilus.scheduler.launch {
                    disguise(it.sender as Player, it.get(entityTypeArg))
                }
            }
        )

        register(unbuilder.proxies(undisguiseCommand.build()))
    }

    private suspend fun disguise(sender: Player, entityType: EntityType) {
        sender.updateCommands()
        this.disguises?.disguise(sender.profile(), entityType)
    }

    private suspend fun undisguise(sender: Player) {
        this.disguises?.disguise(sender.profile(), null)
    }
}