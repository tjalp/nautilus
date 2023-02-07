package net.tjalp.nautilus.command

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.clan
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.profile
import org.bukkit.entity.Player

class ChunkCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val claims; get() = this.nautilus.claims
    private val scheduler; get() = this.nautilus.scheduler
    private val profiles; get() = this.nautilus.profiles
    private val clans; get() = this.nautilus.clans

    init {
        val builder = builder("chunk")

        register(builder.literal("claim").senderType(Player::class.java).handler {
            this.scheduler.launch { claim(it.sender as Player) }
        })
    }

    private suspend fun claim(sender: Player) {
        val chunk = sender.chunk
        val playerClan = sender.profile().clan()

        if (playerClan == null) {
            sender.sendMessage(text("You're not in a clan", RED))
            return
        }

        if (this.claims.hasOwner(chunk)) {
            val owner = this.claims.owner(chunk)!!
            val clan = this.clans.clan(owner)

            if (clan == null) {
                text("This chunk already has an owner", RED)
                return
            }

            sender.sendMessage(
                text("This chunk is already owned by", RED)
                    .appendSpace().append(text(clan.name, clan.theme()))
            )
            return
        }

        val claimed = this.claims.claim(playerClan, chunk)

        if (!claimed) {
            sender.sendMessage(text("Failed to claim chunk", RED))
            return
        }

        sender.sendMessage(text("You've claimed this chunk", GRAY))
    }
}