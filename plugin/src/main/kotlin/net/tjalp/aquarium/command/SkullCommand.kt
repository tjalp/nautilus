package net.tjalp.aquarium.command

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.processing.CommandContainer
import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.mini
import org.bukkit.Material.PLAYER_HEAD
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandContainer
class SkullCommand {

    @CommandMethod("head|skull <target>")
    fun skull(
        sender: Player,
        @Argument("target") target: String
    ) {
        val item = ItemBuilder(PLAYER_HEAD).skull(target).build()

        sender.inventory.addItem(item)
        sender.sendMessage(mini("<green>Gave player head of $target"))
    }
}