package net.tjalp.nautilus.command

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class SpawnCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    init {
        val builder = builder("spawn")
            .senderType(Player::class.java)

        register(builder.handler {
            this.spawn(it.sender as Player)
        })
    }

    private fun spawn(sender: Player) {
        val world = this.nautilus.server.worlds.firstOrNull { it.environment == World.Environment.NORMAL }

        if (world == null) {
            sender.sendMessage(text("The spawn could not be found!"))
            return
        }

        sender.teleportAsync(world.spawnLocation, PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept { accepted ->
            if (accepted) {
                sender.sendMessage(text("Teleported to spawn", GRAY))
                sender.world.playSound(sender.location, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.5f, 1f)
            }
            else sender.sendMessage(text("Failed to teleport to spawn!", RED))
        }
    }
}