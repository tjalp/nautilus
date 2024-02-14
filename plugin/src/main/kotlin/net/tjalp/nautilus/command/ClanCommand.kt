package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent.runCommand
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.clan.ClanInterface
import net.tjalp.nautilus.clan.CreateClanInterface
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.clan
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.profile
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.interfaces.kotlin.paper.asViewer
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

        register(builder.handler {
            this.scheduler.launch { clan(it.sender as Player) }
        })

        register(builder.literal("create").senderType(Player::class.java).argument(optionalNameArg.copy()).handler {
            this.scheduler.launch { create(it.sender as Player, it.getOptional(optionalNameArg).orElse(null)) }
        })

        register(builder.literal("info").argument(optionalNameArg.copy()).senderType(Player::class.java).handler {
            this.scheduler.launch { info(it.sender as Player, it.getOptional(optionalNameArg).orElse(null)) }
        })

        register(builder.literal("disband", "delete", "remove").senderType(Player::class.java).handler {
            this.scheduler.launch { disband(it.sender as Player) }
        })
    }

    private fun clan(sender: Player) {
        val clan = sender.profile().clan()

        if (clan == null) {
            sender.sendMessage(text("You're not in a clan", RED)
                .appendSpace().append(text("CREATE ONE", GREEN, BOLD)
                    .clickEvent(runCommand("/clan create"))
                )
            )
            return
        }

//        ClanInterface(clan).open(sender)
        this.scheduler.launch {
            ClanInterface(null, clan).create().open(sender.asViewer())
            ClanInterface.playOpenSound(sender)
        }
    }

    private suspend fun create(sender: Player, nameArg: String? = null) {
        if (sender.profile().clanId != null) {
            sender.sendMessage(text("You'll have to leave your current clan to make a new one", RED))
            return
        }

        if (nameArg == null) {
//            CreateClanInterface(this.nautilus).open(sender)
            CreateClanInterface(null, this.nautilus).create().open(sender.asViewer())
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
        val chunks = text()
        val worldChunkMaps = clan.claimedChunks.filter { it.chunks.isNotEmpty() }

        worldChunkMaps.forEach {
            val world = this.nautilus.server.getWorld(it.world) ?: return@forEach

            chunks.appendNewline().appendSpace().appendSpace()
            when (world.environment) {
                World.Environment.NORMAL -> chunks.append(text("• Overworld: "))
                World.Environment.NETHER -> chunks.append(text("• The Nether: "))
                World.Environment.THE_END -> chunks.append(text("• The End: "))
                else -> chunks.append(text("• ${world.name} (custom): "))
            }

            it.chunks.forEachIndexed { chunkIndex, chunkId ->
                val chunk = world.getChunkAt(chunkId)

                if (chunkIndex != 0) chunks.append(text(",")).appendSpace()

                chunks.append(text("(${chunk.x} ${chunk.z})").color(clan.theme()))
            }
        }
        worldChunkMaps.ifEmpty { chunks.append(text("None", clan.theme())) }

        val nameComponent = text(clan.name).color(clan.theme())
        val leadersComponent = text().color(clan.theme())
        val membersComponent = text().color(clan.theme())

        if (leaders.isNotEmpty()) {
            leaders.forEachIndexed { index, component ->
                if (index != 0) leadersComponent.append(text(",")).appendSpace()

                leadersComponent.append(component)
            }
        } else {
            leadersComponent.append(text("None", clan.theme()))
        }

        if (members.isNotEmpty()) {
            members.forEachIndexed { index, component ->
                if (index != 0) leadersComponent.append(text(",")).appendSpace()

                membersComponent.append(component)
            }
        } else {
            membersComponent.append(text("None", clan.theme()))
        }

        sender.sendMessage(text().color(TextColor.color(251, 228, 96))
            .append(text("The following information was found about")
                .appendSpace().append(nameComponent).append(text(":")))
            .appendNewline().append(text("• Name: ")).append(nameComponent)
            .appendNewline().append(text("• Leader(s): ")).append(leadersComponent)
            .appendNewline().append(text("• Members: ")).append(membersComponent)
            .appendNewline().append(text("• Claimed chunks: ")).append(chunks)
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