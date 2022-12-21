package net.tjalp.nautilus.player.teleport

import io.papermc.paper.entity.RelativeTeleportFlag.PITCH
import io.papermc.paper.entity.RelativeTeleportFlag.YAW
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent.runCommand
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.displayName
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.profile
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN
import org.bukkit.scheduler.BukkitTask

class PlayerTeleportRequest(val source: Player, val target: Player) : TeleportRequest {

    private val nautilus = Nautilus.get()
    private var expireTask: BukkitTask? = null

    override fun request() {
        if (source == target) {
            this.source.sendMessage(text("You cannot send a request to yourself, silly!", RED))
//            return
        }
        if (requests.any { it.target == target && it.source == source }) {
            this.source.sendMessage(text("You already have an active teleport request to", RED)
                .appendSpace().append(this.target.profile().nameComponent(showPrefix = false, showSuffix = false)))
            return
        }

        requests += this

        this.source.sendMessage(
            text("You sent a teleport request to ", GRAY).append(this.target.profile().nameComponent(showPrefix = false, showSuffix = false))
        )
        this.target.sendMessage(
            text().color(GRAY)
                .appendNewline()
                .append(text("You've received a teleport request from"))
                .appendSpace().append(this.source.profile().nameComponent(showPrefix = false, showSuffix = false))
                .appendNewline().append(text("Would you like to accept this request?"))
                .appendNewline()
                .appendNewline().append(text("YES", GREEN, BOLD).clickEvent(runCommand("/teleportrequest accept \"${this.source.profile().displayName()}\"")))
                .appendSpace().append(text("NO", RED, BOLD).clickEvent(runCommand("/teleportrequest deny \"${this.source.profile().displayName()}\"")))
                .appendNewline()
        )
        this.target.playSound(this.target.location, Sound.ENTITY_ITEM_PICKUP, 10f, 0f)

        this.expireTask = this.nautilus.server.scheduler.runTaskLater(nautilus, Runnable {
            requests -= this

            this.source.sendMessage(
                text("Your teleport request to", GRAY)
                    .appendSpace().append(this.target.profile().nameComponent(showPrefix = false, showSuffix = false))
                    .appendSpace().append(text("has expired"))
            )
            this.target.sendMessage(
                text().color(GRAY)
                    .append(this.source.profile().nameComponent(showPrefix = false, showSuffix = false))
                    .append(text("'s teleport request has expired"))
            )
        }, 90 * 20) // 90 seconds (1.5 min) expiry
    }

    override fun accept() {
        requests -= this
        this.expireTask?.cancel()

        this.target.sendMessage(text("You've accepted", GREEN)
            .appendSpace().append(this.source.profile().nameComponent(showPrefix = false, showSuffix = false))
            .append(text("'s teleport request"))
        )
        this.source.sendMessage(text("Your teleport request has been accepted by", GREEN)
            .appendSpace().append(this.target.profile().nameComponent(showPrefix = false, showSuffix = false)))

        this.source.teleport(this.target.location, PLUGIN, false, true, YAW, PITCH)
    }

    override fun deny() {
        requests -= this
        this.expireTask?.cancel()

        this.target.sendMessage(text("You've denied", RED)
            .appendSpace().append(this.source.profile().nameComponent(showPrefix = false, showSuffix = false))
            .append(text("'s teleport request"))
        )
        this.source.sendMessage(text("Your teleport request has been denied by", RED)
            .appendSpace().append(this.target.profile().nameComponent(showPrefix = false, showSuffix = false)))
    }

    companion object {

        private val requests = mutableListOf<PlayerTeleportRequest>()

        fun requests(): List<PlayerTeleportRequest> = this.requests.toList()
    }
}