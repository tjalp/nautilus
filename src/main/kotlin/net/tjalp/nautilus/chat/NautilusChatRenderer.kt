package net.tjalp.nautilus.chat

import io.papermc.paper.chat.ChatRenderer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.entity.Player

/**
 * The main chat renderer.
 */
object NautilusChatRenderer : ChatRenderer {

    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        return text()
            .append(sourceDisplayName)
            .append(text(" → ").color(color(130, 130, 111)))
            .append(message)
            .build()
    }
}