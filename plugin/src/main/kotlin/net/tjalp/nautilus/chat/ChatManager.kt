package net.tjalp.nautilus.chat

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.registry.DECORATED_CHAT
import net.tjalp.nautilus.util.*
import org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration

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
        fun on(event: AsyncChatEvent) {
            val message = plainText().serialize(event.originalMessage())

            for (viewer in event.viewers()) {
                if (viewer !is Player) continue
                if (!message.lowercase().contains("@${viewer.profile().displayName().lowercase()}")) continue

                val title = title(
                    empty(),
                    text("You've been mentioned in chat!", color(119, 221, 119), ITALIC),
                    times(Duration.ofMillis(250), Duration.ofMillis(750), Duration.ofMillis(500))
                )

                viewer.showTitle(title)

                for(i in 10..20 step 2) {
                    viewer.playSound(viewer.location, ENTITY_EXPERIENCE_ORB_PICKUP, 1f, i / 10f)
                }
            }

            event.renderer(NautilusChatRenderer)
        }

        @EventHandler
        @Suppress("UnstableApiUsage")
        fun on(event: AsyncChatDecorateEvent) {
            val player = event.player()

            if (player != null) {
                val profile = player.profile()
                val decorated = decorateChatMessage(player, event.result())

                if (event.result().compact() != decorated) {
                    event.result(
                        text().color(profile.displayRank().chatColor)
                            .append(decorated)
                            .build()
                    )
                }
            }
        }

        @EventHandler
        fun on(event: PlayerJoinEvent) {
            event.player.addAdditionalChatCompletions(nautilus.server.onlinePlayers.map { "@" + it.profile().displayName() })
        }

        @EventHandler
        fun on(event: PlayerQuitEvent) {
            val profile = event.player.profile()

            for (online in this@ChatManager.nautilus.server.onlinePlayers) {
                online.removeAdditionalChatCompletions(listOf("@${profile.displayName()}"))
            }
        }

        @EventHandler
        fun on(event: ProfileUpdateEvent) {
            val profile = event.profile
            val prev = event.previous ?: return

            if (profile.maskName == prev.maskName) return

            for (online in this@ChatManager.nautilus.server.onlinePlayers) {
                online.removeAdditionalChatCompletions(listOf("@${prev.displayName()}"))
                online.addAdditionalChatCompletions(listOf("@${profile.displayName()}"))
            }
        }
    }
}