package net.tjalp.aquarium.command

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.processing.CommandContainer
import net.tjalp.aquarium.item.TribowItem
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandContainer
class ItemCommand {

    @CommandMethod("item")
    fun item(player: Player) {
        player.inventory.addItem(TribowItem.item)
    }
}