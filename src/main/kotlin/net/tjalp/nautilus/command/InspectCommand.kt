package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.profile.ProfileInterface
import net.tjalp.nautilus.util.GsonHelper
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.interfaces.kotlin.paper.asViewer
import org.litote.kmongo.json

class InspectCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val profiles = this.nautilus.profiles
    private val scheduler = this.nautilus.scheduler

    init {
        val builder = builder("inspect")
        val targetArg = StringArgument.quoted<CommandSender>("target")

        register(
            builder.argument(targetArg).handler {
                this.scheduler.launch { inspect(it.sender, it.get(targetArg)) }
            }
        )
    }

    private suspend fun inspect(sender: CommandSender, target: String) {
        val profile = this.profiles.profile(target)

        if (profile == null) {
            sender.sendMessage(
                text("The profile either does not exist " +
                        "or the user has requested to hide their profile").color(RED)
            )
            return
        }

        if (sender !is Player) {
            sender.sendMessage(GsonHelper.pretty().toJson(JsonParser.parseString(profile.json)))
            return
        }

        ProfileInterface(profile).openWithSound(sender.asViewer())
    }
}