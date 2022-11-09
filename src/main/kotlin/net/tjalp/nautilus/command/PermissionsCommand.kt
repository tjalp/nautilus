package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration.UNDERLINED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.player.profile.data.PermissionInfo
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.primaryRank
import net.tjalp.nautilus.util.ranks
import org.bukkit.command.CommandSender
import org.litote.kmongo.div
import org.litote.kmongo.pull
import org.litote.kmongo.push

class PermissionsCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val scheduler = this.nautilus.scheduler
    private val perms = this.nautilus.perms
    private val profiles = this.nautilus.profiles

    init {
        val builder = builder("permissions", "perms")
        val usernameArg = StringArgument.quoted<CommandSender>("username")
        val rankArg = StringArgument.quoted<CommandSender>("rank")

        register(
            builder.literal("addrank").argument(usernameArg.copy()).argument(rankArg.copy()).handler {
                this.addRank(it.sender, it.get(usernameArg), it.get(rankArg).lowercase())
            }
        )

        register(
            builder.literal("delrank", "deleterank").argument(usernameArg.copy()).argument(rankArg.copy()).handler {
                this.deleteRank(it.sender, it.get(usernameArg), it.get(rankArg).lowercase())
            }
        )

        register(
            builder.literal("ranks").argument(usernameArg.copy()).handler {
                this.ranks(it.sender, it.get(usernameArg))
            }
        )
    }

    private fun addRank(sender: CommandSender, username: String, rankArg: String) {
        if (!this.perms.rankExists(rankArg)) {
            sender.sendMessage(text().color(RED)
                .append(text("Rank "))
                .append(text(rankArg).color(WHITE))
                .append(text(" does not exist!"))
            )
            return
        }

        val rank = this.perms.getRank(rankArg)

        this.scheduler.launch {
            val profile = profiles.profile(username)

            if (profile == null) {
                sender.sendMessage(text().color(RED)
                    .append(text("No profile was found for "))
                    .append(text(username).color(WHITE))
                    .append(text("!"))
                )
                return@launch
            }

            val updatedProfile = profile.update(push(ProfileSnapshot::permissionInfo / PermissionInfo::ranks, rankArg))

            sender.sendMessage(text().color(GRAY)
                .append(text("Added rank "))
                .append(rank.prefix)
                .append(text(" to "))
                .append(updatedProfile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                .append(text(" (now "))
                .append(updatedProfile.primaryRank().prefix)
                .append(text(")!"))
            )
        }
    }

    private fun deleteRank(sender: CommandSender, username: String, rankArg: String) {
        if (!this.perms.rankExists(rankArg)) {
            sender.sendMessage(text().color(RED)
                .append(text("Rank "))
                .append(text(rankArg).color(WHITE))
                .append(text(" does not exist!"))
            )
            return
        }

        val rank = this.perms.getRank(rankArg)

        this.scheduler.launch {
            val profile = profiles.profile(username)

            if (profile == null) {
                sender.sendMessage(text().color(RED)
                    .append(text("No profile was found for "))
                    .append(text(username).color(WHITE))
                    .append(text("!"))
                )
                return@launch
            }

            val updatedProfile = profile.update(pull(ProfileSnapshot::permissionInfo / PermissionInfo::ranks, rankArg))

            sender.sendMessage(text().color(GRAY)
                .append(text("Removed rank "))
                .append(rank.prefix)
                .append(text(" from "))
                .append(updatedProfile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                .append(text(" (now "))
                .append(updatedProfile.primaryRank().prefix)
                .append(text(")!"))
            )
        }
    }

    private fun ranks(sender: CommandSender, usernameArg: String) {
        this.scheduler.launch {
            val profile = profiles.profile(usernameArg)

            if (profile == null) {
                sender.sendMessage(text().color(RED)
                    .append(text("No profile was found for "))
                    .append(text(usernameArg).color(WHITE))
                    .append(text("!"))
                )
                return@launch
            }

            val ranks = profile.ranks().sortedByDescending { it.weight }
            val component = text().color(GRAY)
                .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                .append(text(" has the following ranks (sorted by "))
                .append(text("weight").decoration(UNDERLINED, true))
                .append(text("):"))

            for (rank in ranks) {
                component.append(newline()).append(text("→ ")).append(rank.prefix).append(text(" (${rank.name})"))
            }

            sender.sendMessage(component)
        }
    }
}