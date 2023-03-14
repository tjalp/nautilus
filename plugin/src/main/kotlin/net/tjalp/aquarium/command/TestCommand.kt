package net.tjalp.aquarium.command

import cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorArgument
import org.bukkit.command.CommandSender


class TestCommand: AquariumCommand("test") {

    init {
        val multipleEntitySelector = MultipleEntitySelectorArgument.of<CommandSender>("targets")

        command(builder.literal("give").argument(multipleEntitySelector).handler { context ->

        })
    }
}