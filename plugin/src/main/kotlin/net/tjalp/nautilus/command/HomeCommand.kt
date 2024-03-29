package net.tjalp.nautilus.command

import io.papermc.paper.entity.TeleportFlag
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.home
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND

class HomeCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    init {
        val builder = builder("home")
            .senderType(Player::class.java)
        val setBuilder = builder("sethome").senderType(Player::class.java)

        register(builder.handler {
            this.home(it.sender as Player)
        })

        val set = builder.literal("set").handler { this.set(it.sender as Player) }
        register(set)
        register(setBuilder.proxies(set.build()))
    }

    private fun home(sender: Player) {
        val home = sender.home()

        if (home == null) {
            sender.sendMessage(text("You've not yet set a home!", RED))
            return
        }

        val successful = sender.teleport(home, COMMAND, *TeleportFlag.Relative.values())

        if (!successful) {
            sender.sendMessage(text("Failed to teleport to your home!", RED))
            return
        }

        sender.sendMessage(text("Teleported to your home!", GREEN))
        sender.world.playSound(sender.location, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.5f, 1f)
    }

    private fun set(sender: Player) {
        sender.home(sender.location)

        sender.sendMessage(text("You've set your home to your current location!", GREEN))
    }
}