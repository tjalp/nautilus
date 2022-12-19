package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.registry.PROFILE_COMMAND
import net.tjalp.nautilus.util.has
import net.tjalp.nautilus.util.mini
import net.tjalp.nautilus.util.nameComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.reactivestreams.deleteOneById
import org.litote.kmongo.setValue
import java.util.*
import kotlin.system.measureTimeMillis

class ProfileCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val profiles = this.nautilus.profiles
    private val scheduler = this.nautilus.scheduler

    init {
        val builder = builder("profile")
            .permission { sender -> if (sender is Player) sender has PROFILE_COMMAND else true }
        val usernameArg = StringArgument.quoted<CommandSender>("username")
        val dataArg = StringArgument.builder<CommandSender>("data").greedy().build()

        register(
            builder.argument(usernameArg.copy()).argument(dataArg).handler {
                this.scheduler.launch { data(it.sender, it.get(usernameArg), it.get(dataArg)) }
            }
        )

        register(
            builder.argument(usernameArg.copy()).literal("delete").handler {
                this.scheduler.launch { delete(it.sender, it.get(usernameArg)) }
            }
        )

        register(
            builder.argument(usernameArg.copy()).literal("update").handler {
                this.scheduler.launch { update(it.sender, it.get(usernameArg)) }
            }
        )
    }

    private suspend fun data(sender: CommandSender, username: String, data: String) {
        var profile: ProfileSnapshot?
        val time = measureTimeMillis {
            profile = profiles.profile(username)
            if (profile == null) {
                val uniqueId = withContext(Dispatchers.IO) {
                    nautilus.server.getPlayerUniqueId(username) ?: UUID.nameUUIDFromBytes(username.toByteArray())
                }
                profile = profiles.createProfileIfNonexistent(uniqueId)
            }
            profile = profile!!.update(setValue(ProfileSnapshot::data, data))
        }
        sender.sendMessage(
            text("Set data of ", GRAY)
                .append(profile!!.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                .append(text("'s profile to '"))
                .append(text(profile!!.data ?: return, WHITE))
                .append(text("'"))
                .append(text(" (${time}ms)", WHITE))
        )
    }

    private suspend fun delete(sender: CommandSender, username: String) {
        val time = measureTimeMillis {
            val uniqueId = withContext(Dispatchers.IO) {
                nautilus.server.getPlayerUniqueId(username) ?: UUID.nameUUIDFromBytes(username.toByteArray())
            }
            MongoCollections.profiles.deleteOneById(uniqueId).awaitSingle()
        }

        sender.sendMessage(mini("<gray>Deleted profile of <white>$username</white> (${time}ms)"))
    }

    private suspend fun update(sender: CommandSender, username: String) {
        val profile = profiles.profileIfCached(username)?.update()

        if (profile != null) {
            sender.sendMessage(
                text("Updated ", GRAY)
                    .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                    .append(text("'s profile"))
            )
            return
        }

        sender.sendMessage(text("No cached profile was found for ", RED).append(text(username, WHITE)))
    }
}