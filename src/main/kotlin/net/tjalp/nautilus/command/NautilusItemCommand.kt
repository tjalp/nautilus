package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.arguments.selector.MultipleEntitySelector
import cloud.commandframework.bukkit.parsers.MaterialArgument
import cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorArgument
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.item.NautilusItem
import net.tjalp.nautilus.registry.NAUTILUS_ITEM_COMMAND
import net.tjalp.nautilus.util.ItemBuilder
import net.tjalp.nautilus.util.has
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder

class NautilusItemCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val items = this.nautilus.items

    init {
        val builder = builder("nautilusitem", "ntli", "ntlitem")
            .permission { sender -> if (sender is Player) sender has NAUTILUS_ITEM_COMMAND else true }
        val targets = MultipleEntitySelectorArgument.of<CommandSender>("targets")
        val materialArg = MaterialArgument.of<CommandSender>("material")
        val identifierArg = StringArgument.greedy<CommandSender>("identifier")

        register(builder.literal("give").argument(targets).argument(materialArg).argument(identifierArg).handler {
            this.give(it.sender, it.get(targets), it.get(materialArg), it.get(identifierArg))
        })
    }

    private fun give(sender: CommandSender, selector: MultipleEntitySelector, material: Material?, identifiersArg: String) {
        val entities = selector.entities
        val identifiers = identifiersArg.split(' ').map { it.trim() }

        if (entities.isEmpty()) {
            sender.sendMessage(text("No targets found", RED))
            return
        }

        val fixedIdentifiers = mutableListOf<String>()

        for (identifier in identifiers) {
            if (!this.items.itemExists(identifier)) {
                sender.sendMessage(text("No Nautilus item exists with id '$identifier'", RED))
                continue
            }

            fixedIdentifiers += identifier
        }

        val ntlItem = this.items.getItem(fixedIdentifiers.firstOrNull() ?: return)
        val item = ItemBuilder(material ?: ntlItem.preferredMaterial)
            .customModelData(ntlItem.customModelData)
            .data(NautilusItem.NAUTILUS_ITEM_ID_PDC, ntlItem.identifier)
            .build()

        for (entity in entities) {
            if (entity is InventoryHolder) entity.inventory.addItem(item)
        }

        sender.sendMessage(text("Gave '$fixedIdentifiers' to ${entities.size} target(s)"))
    }
}