package net.tjalp.nautilus.command

import cloud.commandframework.arguments.standard.StringArgument
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.registry.NAUTILUS_ENCHANTMENT_COMMAND
import net.tjalp.nautilus.util.has
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class NautilusEnchantmentCommand(
    override val nautilus: Nautilus
) : NautilusCommand() {

    private val enchantments = nautilus.enchantments

    init {
        val builder = builder("nautilusenchantment", "ntle", "ntlenchant", "nautilusenchant", "ntlenchantment")
            .permission { sender -> if (sender is Player) sender has NAUTILUS_ENCHANTMENT_COMMAND else true }
            .senderType(Player::class.java)
        val identifierArg = StringArgument.greedy<CommandSender>("identifier")

        register(builder.literal("enchant").argument(identifierArg.copy()).handler {
            this.enchant(it.sender as Player, it.get(identifierArg))
        })
    }

    private fun enchant(sender: Player, enchantmentArg: String) {
        val enchantment = this.enchantments.getEnchantment(enchantmentArg)

        if (enchantment == null) {
            sender.sendMessage(text("Enchantment not found", RED))
            return
        }

        this.enchantments.enchant(sender.inventory.itemInMainHand, enchantment)
    }
}