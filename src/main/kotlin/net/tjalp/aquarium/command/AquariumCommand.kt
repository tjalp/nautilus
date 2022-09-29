package net.tjalp.aquarium.command

import cloud.commandframework.Command
import cloud.commandframework.CommandManager
import net.tjalp.aquarium.Aquarium
import org.bukkit.command.CommandSender

/**
 * Using this class, you can create commands
 *
 * @param name The name of the command to register
 * @param aliases List of aliases for the command
 */
abstract class AquariumCommand(name: String, vararg aliases: String) {

    /**
     * The command manager
     */
    val commands = Aquarium.commands

    /**
     * The builder which can be used to create commands with
     */
    val builder = this.commands.commandBuilder(name, *aliases)

    /**
     * Create a new command
     *
     * @param builder The command builder to use
     */
    fun command(builder: Command.Builder<CommandSender>): CommandManager<CommandSender> {
        return commands.command(builder)
    }
}