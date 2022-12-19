package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor.color
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.registry.MASKED_NAMES_SUGGESTIONS
import net.tjalp.nautilus.registry.REAL_NAME_COMMAND
import net.tjalp.nautilus.util.has
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.profile
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RealNameCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    init {
        val builder = builder("realname")
            .permission { sender -> if (sender is Player) sender has REAL_NAME_COMMAND else true }
        val targetArg = StringArgument.builder<CommandSender>("target")
            .greedy()
            .withSuggestionsProvider(MASKED_NAMES_SUGGESTIONS)
            .build()

        register(builder.argument(targetArg).handler {
            this.realName(it.sender, it.get(targetArg))
        })
    }

    private fun realName(sender: CommandSender, target: String) {
        val profiles = mutableListOf<ProfileSnapshot>()

        for (profile in this.nautilus.server.onlinePlayers.map { it.profile() }) {
            if (profile.maskName?.equals(target, ignoreCase = true) == true) profiles += profile
        }

        if (profiles.isEmpty()) {
            sender.sendMessage(text("The mask '$target' is not used by any online players").color(RED))
            return
        }

        val component = text().color(color(251, 228, 96)).append(text("The mask '$target' is used by ${profiles.size} player(s):"))

        for (profile in profiles) {
            component.appendNewline().append(text("• ").append(profile.nameComponent(useMask = false, showSuffix = false)))
        }

        sender.sendMessage(component)
    }
}