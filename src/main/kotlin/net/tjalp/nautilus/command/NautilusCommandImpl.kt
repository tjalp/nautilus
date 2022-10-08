package net.tjalp.nautilus.command

import net.kyori.adventure.text.Component
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.mini

class NautilusCommandImpl(
    override val nautilus: Nautilus
) : NautilusCommand() {

    init {
        val builder = builder("nautilus")

        register(
            builder.literal("test").handler {
                it.sender.sendMessage(Component.text("Test message"))
            }
        )

        register(
            builder.handler {
                it.sender.sendMessage(mini("<rainbow>This is the <u>Nautilus</u> command!"))
            }
        )
    }
}