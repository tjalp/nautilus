package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.UNDERLINED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.GsonHelper
import net.tjalp.nautilus.util.mini
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.ranks
import org.bukkit.command.CommandSender
import org.litote.kmongo.json
import org.litote.kmongo.reactivestreams.deleteOneById
import org.litote.kmongo.reactivestreams.findOneById
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
        val usernameArg = StringArgument.of<CommandSender>("username")
        val dataArg = StringArgument.newBuilder<CommandSender>("data").greedy().build()

        register(
            builder.argument(usernameArg.copy()).handler {
                profile(it.sender, it.get(usernameArg))
            }
        )

        register(
            builder.argument(usernameArg.copy()).argument(dataArg).handler {
                data(it.sender, it.get(usernameArg), it.get(dataArg))
            }
        )

        register(
            builder.argument(usernameArg.copy()).literal("delete").handler {
                delete(it.sender, it.get(usernameArg))
            }
        )

        register(
            builder.argument(usernameArg.copy()).literal("update").handler {
                update(it.sender, it.get(usernameArg))
            }
        )

        register(
            builder.handler {
                profile(it.sender, it.sender.name)
            }
        )
    }

    private fun profile(sender: CommandSender, username: String) {
        this.scheduler.launch {
            val profile: ProfileSnapshot?
            val time = measureTimeMillis {
                profile = profiles.profile(username)
            }
            if (profile == null) {
                sender.sendMessage(mini("<red>Profile does not exist <gray>(${time}ms)"))
            } else {
                val profileJson = GsonHelper.pretty().toJson(JsonParser.parseString(profile.json))
                sender.sendMessage(mini(profileJson + " (${time}ms)").append(newline()))

                val component = text()

                component.append(text("ʀᴀɴᴋs", GRAY, BOLD).append(mini(" <#82826f><!b>→")))
                for (rank in profile.ranks().sortedBy { it.weight }) component.append(newline()).append(rank.prefix)

                component.append(newline()).append(
                    text("ʟᴀsᴛ ᴏɴʟɪɴᴇ", GRAY, BOLD).append(mini(" <#82826f><!b>→"))
                ).append(space()).append(text(Nautilus.TIME_FORMAT.format(profile.lastOnline), WHITE).decoration(BOLD, false))

                component.append(newline()).append(
                    text("ᴅᴀᴛᴀ", GRAY, BOLD).append(mini(" <#82826f><!b>→"))
                ).append(space()).append(text(profile.data ?: "", WHITE).decoration(BOLD, false))

                component.append(newline()).append(
                    text("ʟᴀsᴛ ᴋɴᴏᴡɴ ɴᴀᴍᴇ", GRAY, BOLD).append(mini(" <#82826f><!b>→"))
                ).append(space()).append(text(profile.lastKnownName, WHITE).decoration(BOLD, false))

                sender.sendMessage(component.build())
            }
        }
    }

    private fun data(sender: CommandSender, username: String, data: String) {
        this.scheduler.launch {
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
                    .append(profile!!.nameComponent(useMask = false, showSuffix = false))
                    .append(text("'s profile to '"))
                    .append(text(profile!!.data ?: return@launch, WHITE))
                    .append(text("'"))
                    .append(text(" (${time}ms)", WHITE))
            )
        }
    }

    private fun delete(sender: CommandSender, username: String) {
        this.scheduler.launch {
            val time = measureTimeMillis {
                val uniqueId = withContext(Dispatchers.IO) {
                    nautilus.server.getPlayerUniqueId(username) ?: UUID.nameUUIDFromBytes(username.toByteArray())
                }
                MongoCollections.profiles.deleteOneById(uniqueId).awaitSingle()
            }

            sender.sendMessage(mini("<gray>Deleted profile of <white>$username</white> (${time}ms)"))
        }
    }

    private fun update(sender: CommandSender, username: String) {
        this.scheduler.launch {
            val profile: ProfileSnapshot?
            val time = measureTimeMillis {
                val uniqueId = withContext(Dispatchers.IO) {
                    nautilus.server.getPlayerUniqueId(username) ?: UUID.nameUUIDFromBytes(username.toByteArray())
                }
                profile = MongoCollections.profiles.findOneById(uniqueId).awaitFirstOrNull()

                if (profile != null) profiles.onProfileUpdate(profile)
            }

            if (profile != null) {
                sender.sendMessage(
                    text("Updated ", GRAY)
                        .append(profile.nameComponent(useMask = false, showSuffix = false))
                        .append(text("'s profile")).append(space())
                        .append(text("(${time}ms)", WHITE))
                )
            }
            else sender.sendMessage(mini("<red>No profile was found for '<white>$username</white>' <gray>(${time}ms)"))
        }
    }
}