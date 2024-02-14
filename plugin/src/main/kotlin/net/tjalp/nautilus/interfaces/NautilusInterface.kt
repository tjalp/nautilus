package net.tjalp.nautilus.interfaces

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.tjalp.nautilus.util.ItemBuilder
import net.tjalp.nautilus.util.playClickSound
import org.bukkit.Material
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.pane.Pane
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement

interface NautilusInterface<T : Pane> {

    fun create(): Interface<T, *>

    fun parent(): Interface<*, PlayerViewer>?

    fun backElement(): ItemStackElement<T> {
        return ItemStackElement<T>(ItemBuilder(Material.ARROW)
            .name(text("← Back", color(251, 228, 96)))
            .build()
        ) {
            it.viewer().player().playClickSound()
            parent()?.open(it.viewer())
        }
    }
}