package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import kotlinx.coroutines.launch
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.mask.MaskContainer
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.registry.MASK_COMMAND
import net.tjalp.nautilus.util.has
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
    private val masking = this.nautilus.masking

    init {
        val builder = builder("mask", "nick")
            .senderType(Player::class.java)
            .permission { sender -> if (sender is Player) sender has MASK_COMMAND else true }
        val unbuilder = builder("unmask", "unnick").senderType(Player::class.java)
        val nameArg = StringArgument.quoted<CommandSender>("username")
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

        val unmask = builder.literal("none", "clear", "reset").handler { this.none(it.sender as Player) }
            .apply { register(this) }
        register(unbuilder.proxies(unmask.build()))

        register(
            builder.literal("name", "username").argument(nameArg).handler {
                this.name(it.sender as Player, it.get(nameArg))
            }
        )

        register(
            builder.handler {
                this.mask(it.sender as Player)
            }
        )
    }

    private fun rank(sender: Player, rank: String) {
        if (!this.perms.rankExists(rank)) {
            sender.sendMessage(mini("<red>Rank <white>$rank</white> does not exist!"))
            return
        }

        val permRank = this.perms.getRank(rank)

        this.scheduler.launch { masking.mask(sender.profile(), rank = permRank) }
    }

    private fun skin(sender: Player, skin: String) {
        this.scheduler.launch { masking.mask(sender.profile(), skin = skin) }
    }

    private fun none(sender: Player) {
        val profile = sender.profile()

        this.scheduler.launch {
            val time = measureTimeMillis {
                profile.update(
                    setValue(ProfileSnapshot::maskName, null),
                    setValue(ProfileSnapshot::maskRank, null),
                    setValue(ProfileSnapshot::maskSkin, null)
                )
            }

            sender.sendMessage(mini("<gray>You no longer have a mask <white>(${time}ms)"))
        }
    }

    private fun name(sender: Player, username: String) {
        if (username.length > 16) {
            sender.sendMessage(mini("<red>The username cannot be longer than 16 characters, you have ${username.length}!"))
            return
        }

        this.scheduler.launch { masking.mask(sender.profile(), username = username) }
    }

    private fun mask(sender: Player) {
        MaskContainer().open(sender)
    }
}