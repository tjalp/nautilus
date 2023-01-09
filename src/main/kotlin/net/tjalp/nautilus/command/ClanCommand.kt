package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.clan
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.profile
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.setValue
import kotlin.system.measureTimeMillis

class ClanCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val clans; get() = this.nautilus.clans
    private val scheduler; get() = this.nautilus.scheduler

    init {
        val builder = builder("clan", "clans")
        val nameArg = StringArgument.greedy<CommandSender>("name")
        val optionalNameArg = StringArgument.builder<CommandSender>("name").quoted().asOptional().build()

        register(builder.literal("create").senderType(Player::class.java).argument(nameArg.copy()).handler {
            this.scheduler.launch { create(it.sender as Player, it.get(nameArg)) }
        })

        register(builder.literal("info").argument(optionalNameArg).senderType(Player::class.java).handler {
            this.scheduler.launch { info(it.sender as Player, it.getOptional(optionalNameArg).orElse(null)) }
        })

        register(builder.literal("disband", "delete", "remove").senderType(Player::class.java).handler {
            this.scheduler.launch { disband(it.sender as Player) }
        })
    }

    private suspend fun create(sender: Player, nameArg: String) {
        if (sender.profile().clanId != null) {
            sender.sendMessage(text("You'll have to leave your current clan to make a new one", RED))
            return
        }

        val clan = this.clans.createClan(leader = sender.uniqueId, name = nameArg)
        sender.profile().update(setValue(property = ProfileSnapshot::clanId, value = clan.id))

        sender.sendMessage(text("You've created a clan with the name ${clan.name}", GRAY))
    }

    private suspend fun info(sender: Player, target: String? = null) {
        val profile = sender.profile()
        val clan = if (target == null) profile.clan() else this.clans.clan(target)

        if (clan == null && target == null) {
            sender.sendMessage(text("You're not in a clan!", RED))
            return
        }

        if (clan == null) {
            sender.sendMessage(text("The clan you requested does not exist", RED))
            return
        }

        val leaders = this.nautilus.profiles.profiles(*clan.leaders.toTypedArray())
            .map { it.nameComponent(useMask = false, showPrefix = false, showSuffix = false) }
        val members = this.nautilus.profiles.profiles(*clan.members.toTypedArray())
            .map { it.nameComponent(useMask = false, showPrefix = false, showSuffix = false) }

        val leadersComponent = text()
        val membersComponent = text()

        leaders.forEachIndexed { index, component ->
            if (index != 0) leadersComponent.append(text(",")).appendSpace()

            leadersComponent.append(component)
        }
        members.forEachIndexed { index, component ->
            if (index != 0) leadersComponent.append(text(",")).appendSpace()

            membersComponent.append(component)
        }

        sender.sendMessage(text().color(TextColor.color(251, 228, 96))
            .append(text("The following information was found about ${clan.name}:"))
            .appendNewline().append(text("• Name: ${clan.name}"))
            .appendNewline().append(text("• Leader(s): ")).append(leadersComponent)
            .appendNewline().append(text("• Members: ")).append(membersComponent)
        )
    }

    private suspend fun disband(sender: Player) {
        val profile = sender.profile()
        val clan = profile.clan()

        if (clan == null) {
            sender.sendMessage(text("You're not in a clan!", RED))
            return
        }

        if (sender.uniqueId !in clan.leaders) {
            sender.sendMessage(text("You must be the leader of the clan to disband it!", RED))
            return
        }

        val time = measureTimeMillis { this.clans.disbandClan(clan) }

        sender.sendMessage(text("Disbanded your clan (${time}ms)", GRAY))
    }
}