package net.tjalp.nautilus.command

import cloud.commandframework.Command
import net.tjalp.nautilus.Nautilus
import org.bukkit.command.CommandSender

/**
 * A basic Nautilus command, all commands
 * should extend this class.
 */
abstract class NautilusCommand {

    abstract val nautilus: Nautilus

    /**
     * Get a new builder for the specified command
     *
     * @param name The command to make a new builder for
     * @param aliases The aliases for this command
     * @return The [Command.Builder] for this command
     */
    fun builder(name: String, vararg aliases: String): Command.Builder<CommandSender> {
        return this.nautilus.commands.commandBuilder(name, *aliases)
    }

    /**
     * Register a [Command.Builder]
     */
    fun register(builder: Command.Builder<CommandSender>) {
        this.nautilus.commands.command(builder)
    }
}