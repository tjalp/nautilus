package net.tjalp.aquarium.command

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.processing.CommandContainer
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.context.CommandContext
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.registry.COMMAND_GIVE_ITEM
import net.tjalp.aquarium.util.getFormattedName
import net.tjalp.aquarium.util.mini
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandContainer
@CommandPermission(COMMAND_GIVE_ITEM)
class GiveItemCommand {

    @CommandMethod("giveitem <item> [target]")
    fun item(
        sender: Player,
        @Argument(value = "item", suggestions = "custom_item") itemIdentifier: String,
        @Argument(value = "target") target: Player?
    ) {
        val player = target ?: sender
        val item = Aquarium.itemRegistry.getItem(itemIdentifier)

        if (item == null) {
            sender.sendMessage(mini("<red>That item does not exist!"))
            return
        }

        val bukkitItem = item.item

        player.inventory.addItem(bukkitItem)

        sender.sendMessage(
            text().color(NamedTextColor.GREEN)
                .append(text("Added "))
                .append(bukkitItem.displayName())
                .append(text(" to "))
                .append(player.getFormattedName(useSuffix = false))
                .append(text("'s inventory"))
        )
    }

    @Suggestions("custom_item")
    fun suggestCustomItem(context: CommandContext<CommandSender>, input: String): List<String> {
        return Aquarium.itemRegistry.items.map { it.identifier }.filter { it.startsWith(input) }.sortedBy { it }
    }
}