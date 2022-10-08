package net.tjalp.nautilus.chat

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.registry.DECORATED_CHAT
import net.tjalp.nautilus.util.has
import net.tjalp.nautilus.util.profile
import net.tjalp.nautilus.util.register
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
    fun decorateChatMessage(player: Player?, message: TextComponent): Component {
        val component = text()
        val mini = miniMessage()

        if (player != null) {
            val profile = player.profile()

            if (profile has DECORATED_CHAT) {
                component.append(
                    mini.deserialize(
                        message.content()
                    )
                )
            }
        } else {
            component.append(
                mini.deserialize(
                    message.content()
                )
            )
        }

        return component.build().compact()
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

            if (player != null && nautilus.perms.has(player.profile(), DECORATED_CHAT)) {
                val decorated = decorateChatMessage(player, event.result() as TextComponent)

                if (event.result().compact() != decorated) event.result(decorated)
            }
        }
    }
}