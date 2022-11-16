package net.tjalp.nautilus.chat

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.registry.DECORATED_CHAT
import net.tjalp.nautilus.util.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * This class manages all chat-related
 * stuff, like formatting, private messaging,
 * broadcasts etc.
 */
class ChatManager(
    val nautilus: Nautilus
) {

    init {
        ChatListener().register()
    }

    /**
     * Decorate a chat component
     *
     * @param player The player context, may be null
     * @param message The message to decorate
     * @return The decorated message in [Component]
     */
    fun decorateChatMessage(player: Player?, message: Component, useChatColor: Boolean = true): Component {
        val component = text()
        val mini = miniMessage()

        if (player != null) {
            val profile = player.profile()
            val decoratedContent = mini.deserialize(plainText().serialize(message))
            if (useChatColor) component.color(profile.displayRank().chatColor)

            if (profile has DECORATED_CHAT) component.append(decoratedContent)
            else component.append(message)
        } else {
            component.append(
                mini.deserialize(
                    plainText().serialize(message)
                )
            )
        }

        return component.build().compact()
    }

    /**
     * Format a chat message
     *
     * @param player The player that should be used for formatting
     * @param message The message to add
     * @return A formatted [Component]
     */
    fun formatChatMessage(player: Player, message: Component): Component {
        val profile = player.profile()
        val name = profile.nameComponent(showSuffix = false)

        return text()
            .append(name)
            .append(text(" → ").color(color(130, 130, 111)))
            .append(message)
            .build()
    }

    /**
     * The chat listener that will manage everything
     * to with chat
     */
    private inner class ChatListener : Listener {

        @EventHandler
        @Suppress("UnstableApiUsage")
        fun on(event: AsyncChatDecorateEvent) {
            val player = event.player()

            if (player != null && player has DECORATED_CHAT) {
                val profile = player.profile()
                val decorated = decorateChatMessage(player, event.result(), useChatColor = false)

                if (event.result().compact() != decorated) {
                    event.result(
                        text().color(profile.displayRank().chatColor)
                            .append(decorated)
                            .build()
                    )
                }
            }
        }
    }
}