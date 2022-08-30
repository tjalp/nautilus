package net.tjalp.aquarium.command

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.processing.CommandContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.registry.COMMAND_CHUNK_INFO
import net.tjalp.aquarium.registry.COMMAND_CHUNK_MASTER
import net.tjalp.aquarium.util.getMaster
import net.tjalp.aquarium.util.getMasteredChunks
import net.tjalp.aquarium.util.hasMaster
import net.tjalp.aquarium.util.setMaster
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandContainer
class ChunkCommand {

    val chunks; get() = Aquarium.chunkManager

    @CommandMethod("chunk info")
    @CommandPermission(COMMAND_CHUNK_INFO)
    fun info(player: Player) {
        val chunk = player.chunk

        if (!chunk.hasMaster()) {
            player.sendMessage(Component.text("This chunk has no master. You can master it via ")
                .color(NamedTextColor.RED)
                .append(Component.text("/chunk master").color(NamedTextColor.YELLOW))
                .append(Component.text("!"))
            )
            return
        } else {
            val masterUniqueId = chunk.getMaster()!!
            val master = Aquarium.loader.server.getPlayer(masterUniqueId)
            val masterComponent = master?.name() ?: Component.text(masterUniqueId.toString())

            player.sendMessage(Component.text("This chunk is mastered by ").color(NamedTextColor.GREEN)
                .append(masterComponent)
                .append(Component.text("!"))
            )
        }
    }

    @CommandMethod("chunk master")
    @CommandPermission(COMMAND_CHUNK_MASTER)
    fun master(player: Player) {
        player.chunk.setMaster(player)

        player.sendMessage(
            Component.text("You now master this chunk. ").color(NamedTextColor.GREEN)
                .append(Component.text("You also master ${player.getMasteredChunks().size - 1} other chunks!").color(NamedTextColor.YELLOW)
        ))
    }
}