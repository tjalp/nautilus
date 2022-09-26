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
import net.tjalp.aquarium.util.ItemBuilder
import net.tjalp.aquarium.util.getFormattedName
import net.tjalp.aquarium.util.mini
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

@Suppress("UNUSED")
@CommandContainer
@CommandPermission(COMMAND_GIVE_ITEM) // todo: change permission
class CustomItemCommand {

    @CommandMethod("customitem give <target> <item>")
    fun give(
        sender: CommandSender,
        @Argument(value = "target") target: Player,
        @Argument(value = "item", suggestions = "custom_item") itemIdentifier: String
    ) {
        val item = Aquarium.itemRegistry.getItem(itemIdentifier)
        val targetName = if (target is Player) {
            target.getFormattedName(useSuffix = false)
        } else target.customName() ?: target.name()

        if (item == null) {
            sender.sendMessage(mini("<red>That item does not exist!"))
            return
        }

        val bukkitItem = item.item

        if (target is InventoryHolder) target.inventory.addItem(bukkitItem)

        sender.sendMessage(
            text().color(NamedTextColor.GREEN)
                .append(text("Added "))
                .append(bukkitItem.displayName())
                .append(text(" to "))
                .append(targetName)
                .append(text("'s inventory"))
        )
    }

    @CommandMethod("customitem enchant <enchantment> [amplifier]")
    fun enchant(
        sender: Player,
        @Argument(value = "enchantment") enchantment: Enchantment,
        @Argument(value = "amplifier", defaultValue = "1") amplifier: Int
    ) {
        val mainHand = sender.inventory.itemInMainHand
        val currentItem: ItemStack = if (mainHand.type == Material.AIR) {
            sender.inventory.itemInOffHand
        } else mainHand

        if (currentItem.type == Material.AIR) {
            sender.sendMessage(mini("<red>You are not holding an item!"))
            return
        }

        ItemBuilder(currentItem).enchant(enchantment, amplifier) // not building, otherwise the enchantment would be removed

        sender.sendMessage(mini("<green>Applied enchantment ")
            .append(enchantment.displayName(amplifier))
            .append(text(" to your current item")))
    }

    @Suggestions("custom_item")
    fun suggestCustomItem(context: CommandContext<CommandSender>, input: String): List<String> {
        return Aquarium.itemRegistry.items.map { it.identifier }.filter { it.startsWith(input) }.sortedBy { it }
    }
}