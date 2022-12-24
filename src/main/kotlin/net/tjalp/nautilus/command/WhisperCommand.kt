package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.registry.DISPLAY_NAME_SUGGESTIONS
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.profile
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Duration

class WhisperCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    init {
        val builder = builder("whisper", "w", "msg", "message", "dm", "pm")
        val targetArg = StringArgument.builder<CommandSender>("target")
            .quoted()
            .withSuggestionsProvider(DISPLAY_NAME_SUGGESTIONS)
            .build()
        val messageArg = StringArgument.builder<CommandSender>("message").greedy().build()

        register(builder.argument(targetArg).argument(messageArg).handler {
            this.whisper(it.sender, it.get(targetArg), it.get(messageArg))
        })
    }

    private fun whisper(sender: CommandSender, targetArg: String, messageArg: String) {
        val target = this.nautilus.masking.playerFromDisplayName(targetArg)
        val senderComponent = if (sender is Player) sender.profile().nameComponent(showPrefix = false, showSuffix = false)
            else text("Console")
        val messageComponent = this.nautilus.chat.decorateChatMessage(sender as? Player, text(messageArg), useChatColor = false)

        if (target == null) {
            sender.sendMessage(text("No target found", RED))
            return
        }

        if (target == sender) {
            sender.sendMessage(text("You can't send a private message to yourself!", RED))
            return
        }

        target.sendMessage(text().color(color(167, 199, 231))
            .append(text("From").color(color(248, 200, 220)))
            .appendSpace().append(senderComponent)
            .appendSpace().append(text("→").color(color(130, 130, 111)))
            .appendSpace().append(messageComponent)
        )
        sender.sendMessage(text().color(color(167, 199, 231))
            .append(text("To").color(color(248, 200, 220)))
            .appendSpace().append(target.profile().nameComponent(showPrefix = false, showSuffix = false))
            .appendSpace().append(text("→").color(color(130, 130, 111)))
            .appendSpace().append(messageComponent)
        )

        target.showTitle(title(
            empty(),
            text("You've received a private message!", color(119, 221, 119), ITALIC),
            times(Duration.ofMillis(250), Duration.ofMillis(750), Duration.ofMillis(500))
        ))

        for(i in 10..20 step 2) {
            target.playSound(target.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, i / 10f)
        }
    }
}