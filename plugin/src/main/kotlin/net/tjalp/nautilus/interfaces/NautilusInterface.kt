package net.tjalp.nautilus.interfaces

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.tjalp.nautilus.util.ItemBuilder
import net.tjalp.nautilus.util.playClickSound
import org.bukkit.Material
import org.incendo.interfaces.next.drawable.Drawable.Companion.drawable
import org.incendo.interfaces.next.element.StaticElement
import org.incendo.interfaces.next.interfaces.Interface

interface NautilusInterface {

    fun create(): Interface<*>

    fun backElement(): StaticElement {
        return StaticElement(drawable(
            ItemBuilder(Material.ARROW)
                .name(text("← Back", color(251, 228, 96)))
                .build()
            )
        ) {
            it.player.playClickSound()
            it.view.back()
        }
    }
}