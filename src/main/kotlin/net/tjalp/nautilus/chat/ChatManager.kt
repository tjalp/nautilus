package net.tjalp.nautilus.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.register
import org.bukkit.entity.Player

/**
 * This class manages all chat-related
 * stuff, like formatting, private messaging,
 * broadcasts etc.
 */
class ChatManager(
    val nautilus: Nautilus
) {

    init {
        ChatListener(this).register()
    }

    /**
     * Decorate a chat component
     */
    fun decorateChatMessage(player: Player?, component: TextComponent): Component {
        return text("")
    }
}