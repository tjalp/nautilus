package net.tjalp.aquarium.command

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.processing.CommandContainer
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.context.CommandContext
import net.kyori.adventure.text.Component
import net.tjalp.aquarium.Aquarium
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandContainer
class ItemCommand {

    @CommandMethod("item <item>")
    fun item(player: Player, @Argument(value = "item", suggestions = "custom_item") itemIdentifier: String) {
        val item = Aquarium.itemRegistry.getItem(itemIdentifier)

        if (item == null) {
            player.sendMessage(Component.text("That item does not exist!"))
            return
        }

        player.inventory.addItem(item.item)
    }

    @Suggestions("custom_item")
    fun suggestCustomItem(context: CommandContext<CommandSender>, input: String): List<String> {
        return Aquarium.itemRegistry.items.map { it.identifier }.filter { it.startsWith(input) }.sortedBy { it }
    }
}