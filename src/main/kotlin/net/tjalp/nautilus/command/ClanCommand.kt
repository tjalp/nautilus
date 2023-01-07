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
        val optionalNameArg = StringArgument.optional<CommandSender>("name")

        register(builder.literal("create").senderType(Player::class.java).argument(nameArg.copy()).handler {
            this.scheduler.launch { create(it.sender as Player, it.get(nameArg)) }
        })

        register(builder.literal("info").argument(optionalNameArg).senderType(Player::class.java).handler {
            this.scheduler.launch { info(it.sender as Player, it.getOptional(optionalNameArg).orElse(null)) }
        })

        register(builder.literal("disband").senderType(Player::class.java).handler {
            this.scheduler.launch { disband(it.sender as Player) }
        })
    }

    private suspend fun create(sender: Player, nameArg: String) {
        if (sender.profile().clanId != null) {
            sender.sendMessage(text("You'll have to leave your current clan to make a new one", RED))
            return
        }

        val clan = this.clans.createClan(leader = sender.uniqueId, name = nameArg)
        sender.profile().update(setValue(ProfileSnapshot::clanId, clan.id))

        sender.sendMessage(text("You've created a clan with the name ${clan.name}", GRAY))
    }

    private suspend fun info(sender: Player, target: String? = null) {
        val profile = sender.profile()
        val clan = if (target == null) profile.clan() else return

        if (clan == null) {
            sender.sendMessage(text("You're not in a clan!", RED))
            return
        }

        sender.sendMessage(text().color(TextColor.color(251, 228, 96))
            .append(text("The following information was found about your clan:"))
            .appendNewline().append(text("• Name: ${clan.name}"))
            .appendNewline().append(text("• Leader(s): ${clan.leaders}"))
            .appendNewline().append(text("• Members: ${clan.members}"))
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