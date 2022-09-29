package net.tjalp.nautilus.chat

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * The chat listener listens for everything
 * that happens in chat, like sending messages,
 * receiving messages etc.
 */
class ChatListener(
    private val chat: ChatManager
) : Listener {

    @EventHandler
    @Suppress("UnstableApiUsage")
    fun on(event: AsyncChatDecorateEvent) {
        event.result(this.chat.decorateChatMessage(event.player(), event.result() as TextComponent))
    }
}