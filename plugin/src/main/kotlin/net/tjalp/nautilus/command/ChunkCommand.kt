package net.tjalp.nautilus.command

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.clan
import net.tjalp.nautilus.util.neighbors
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

        register(builder.literal("unclaim").senderType(Player::class.java).handler {
            this.scheduler.launch { unclaim(it.sender as Player) }
        })

        register(builder.literal("unclaim").literal("all").senderType(Player::class.java).handler {
            this.scheduler.launch { unclaimAll(it.sender as Player) }
        })
    }

    private suspend fun claim(sender: Player) {
        val chunk = sender.chunk
        val playerClan = sender.profile().clan()

        if (playerClan == null) {
            sender.sendMessage(text("You're not in a clan", RED))
            return
        }

        if (playerClan.claimedChunksCount() >= playerClan.chunkLimit()) {
            sender.sendMessage(text("Your clan cannot claim any more chunks (${playerClan.claimedChunksCount()}/${playerClan.chunkLimit()})", RED))
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

        val claimedChunks = playerClan.claimedChunks
        val hasChunksInWorld = claimedChunks.any { it.world == chunk.world.uid && it.chunks.isNotEmpty() }
        val isNextToTerritory = chunk.neighbors().any { this.claims.owner(it) == playerClan.id }

        if (hasChunksInWorld && !isNextToTerritory) {
            sender.sendMessage(text("You must claim a chunk next to your clan's territory in this world", RED))
            return
        }

        val claimed = this.claims.claim(playerClan, chunk)

        if (!claimed) {
            sender.sendMessage(text("Failed to claim chunk", RED))
            return
        }

        val newClan = sender.profile().clan()!!

        sender.sendMessage(text("You've claimed this chunk (${newClan.claimedChunksCount()}/${newClan.chunkLimit()})", GRAY))
    }

    private suspend fun unclaim(sender: Player) {
        val chunk = sender.chunk
        val playerClan = sender.profile().clan()

        if (playerClan == null) {
            sender.sendMessage(text("You're not in a clan", RED))
            return
        }

        if (this.claims.owner(chunk) != playerClan.id) {
            sender.sendMessage(text("This chunk isn't claimed by your clan", RED))
            return
        }

        this.claims.unclaim(playerClan, chunk)
        sender.sendMessage(text("Your clan no longer owns this chunk", GRAY))
    }

    private suspend fun unclaimAll(sender: Player) {
        val playerClan = sender.profile().clan()

        if (playerClan == null) {
            sender.sendMessage(text("You're not in a clan", RED))
            return
        }

        this.claims.unclaimAll(playerClan)
        sender.sendMessage(text("Your clan no longer owns any chunks", GRAY))
    }
}