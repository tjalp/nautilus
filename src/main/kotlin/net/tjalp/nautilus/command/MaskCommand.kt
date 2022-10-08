package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.mini
import net.tjalp.nautilus.util.profile
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.setValue
import kotlin.system.measureTimeMillis

class MaskCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val scheduler = this.nautilus.scheduler
    private val perms = this.nautilus.perms

    init {
        val builder = builder("mask", "nick").senderType(Player::class.java)
        val nameArg = StringArgument.of<CommandSender>("username")
        val rankArg = StringArgument.of<CommandSender>("rank")
        val skinArg = StringArgument.of<CommandSender>("skin")

        register(
            builder.literal("rank").argument(rankArg).handler {
                this.rank(it.sender as Player, it.get(rankArg).lowercase())
            }
        )

        register(
            builder.literal("skin").argument(skinArg).handler {
                this.skin(it.sender as Player, it.get(skinArg))
            }
        )

        register(
            builder.literal("none").handler {
                this.none(it.sender as Player)
            }
        )

        register(
            builder.argument(nameArg).handler {
                this.mask(it.sender as Player, it.get(nameArg))
            }
        )
    }

    private fun rank(sender: Player, rank: String) {
        val profile = sender.profile()

        if (!this.perms.rankExists(rank)) {
            sender.sendMessage(mini("<red>Rank <white>$rank</white> does not exist!"))
            return
        }

        val permRank = this.perms.getRank(rank)

        this.scheduler.launch {
            val time = measureTimeMillis {
                profile.update(setValue(ProfileSnapshot::maskRank, rank))
            }

            sender.sendMessage(
                text("Set your mask rank to ", GRAY)
                    .append(permRank.prefix)
                    .append(text(" (${time}ms)"))
            )
        }
    }

    private fun skin(sender: Player, skin: String) {
        val usernameProfile = nautilus.server.createProfile(skin)

        this.scheduler.launch {
            val completed: Boolean
            val time = measureTimeMillis {
                withContext(Dispatchers.IO) {
                    completed = usernameProfile.complete()
                }
            }

            if (!completed) {
                sender.sendMessage(mini("<red>Operation failed. Is there no user with that name? <gray>(${time}ms)"))
                return@launch
            }

            sender.playerProfile = sender.playerProfile.apply {
                setProperty(usernameProfile.properties.first { it.name == "textures" })
            }

            sender.sendMessage(mini("<gray>Set your skin to <white>${usernameProfile.name ?: skin}</white> (${time}ms)"))
        }
    }

    private fun none(sender: Player) {
        val profile = sender.profile()

        this.scheduler.launch {
            val time = measureTimeMillis {
                profile.update(
                    setValue(ProfileSnapshot::maskName, null),
                    setValue(ProfileSnapshot::maskRank, null)
                )
            }

            sender.sendMessage(mini("<gray>You no longer have a mask <white>(${time}ms)"))
        }
    }

    private fun mask(sender: Player, username: String) {
        val profile = sender.profile()

        this.scheduler.launch {
            val updatedProfile: ProfileSnapshot
            val time = measureTimeMillis {
                updatedProfile = profile.update(setValue(ProfileSnapshot::maskName, username))
            }

            sender.sendMessage(
                mini("<gray>Set your mask name to <white>${updatedProfile.maskName}</white> (${time}ms)")
            )
        }
    }
}