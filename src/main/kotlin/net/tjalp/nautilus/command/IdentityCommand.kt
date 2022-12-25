package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.player.profile.GoogleUser
import net.tjalp.nautilus.registry.IDENTITY_COMMAND
import net.tjalp.nautilus.registry.REAL_NAME_SUGGESTIONS
import net.tjalp.nautilus.util.has
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.primaryRank
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.eq

class IdentityCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    init {
        val builder = builder("identity")
            .permission { sender -> if (sender is Player) sender has IDENTITY_COMMAND else true }
        val targetArg = StringArgument.builder<CommandSender>("target")
            .quoted()
            .withSuggestionsProvider(REAL_NAME_SUGGESTIONS)
            .build()

        register(builder.argument(targetArg).handler {
            this.nautilus.scheduler.launch { identity(it.sender, it.get(targetArg)) }
        })
    }

    private suspend fun identity(sender: CommandSender, targetArg: String) {
        val profile = this.nautilus.profiles.profile(targetArg)

        if (profile == null) {
            sender.sendMessage(text("No profile found with name $targetArg", RED))
            return
        }

        val googleUser = MongoCollections.googleUsers.find(
            GoogleUser::minecraftUuid eq profile.uniqueId
        ).awaitFirstOrNull()

        if (googleUser == null) {
            sender.sendMessage(text().color(RED)
                .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                .appendSpace().append(text("does not have a (valid) Google user linked to their profile"))
            )
            return
        }

        val color = profile.primaryRank().nameColor
        val firstName = text(googleUser.firstName.toString(), color)
        val lastName = text(googleUser.lastName.toString(), color)
        val email = text(googleUser.email.toString(), color)

        val builder = text().color(TextColor.color(251, 228, 96))
            .append(text("The following information was found about"))
            .appendSpace().append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
            .append(text(":"))
            .appendNewline().append(text("• First name: ").append(firstName))
            .appendNewline().append(text("• Last name: ").append(lastName))
            .appendNewline().append(text("• E-mail: ").append(email))

        sender.sendMessage(builder)
    }
}