package net.tjalp.nautilus.chat

import io.papermc.paper.chat.ChatRenderer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextColor.color
import net.tjalp.nautilus.registry.REAL_NAME_COMMAND
import net.tjalp.nautilus.util.*
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

/**
 * The main chat renderer.
 */
object NautilusChatRenderer : ChatRenderer {

    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        val display = text().append(sourceDisplayName)
//        var modifiedMessage = message

        if (source.profile().maskName != null
            && (viewer is ConsoleCommandSender || (viewer is Player && viewer has REAL_NAME_COMMAND))
            ) {
            display.appendSpace()
                .append(text("(").color(GRAY)
                    .append(source.profile().nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                    .append(text(")")))
        }
//
//        if (viewer is Player) {
//            val regex = Regex("@${viewer.profile().displayName()}", RegexOption.IGNORE_CASE)
//
//            if (message is TextComponent) {
//                modifiedMessage = message.toBuilder().mapChildrenDeep {
//                    val component = it as? TextComponent ?: return@mapChildrenDeep it
//                    val content = component.content()
//                    val match = regex.find(content) ?: return@mapChildrenDeep it
//                    val insert = text()
//
//                    val prefix = component.content(content.substring(0, match.range.first))
//                    val suffix = component.content(content.substring(match.range.last + 1))
//
//                    insert.append(prefix)
//                    insert.append(text("@").append(viewer.profile().nameComponent(useMask = true, showPrefix = false, showSuffix = false)))
//                    insert.append(suffix)
//
//                    insert.build()
//                }.build()
//            }
//        }

        return text()
            .append(display)
            .append(text(" → ").color(color(130, 130, 111)))
            .append(message)
            .build().compact()
    }
}