package net.tjalp.nautilus.interfaces

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.ItemBuilder
import net.tjalp.nautilus.util.playClickSound
import org.bukkit.Material
import org.bukkit.entity.Player
import org.geysermc.cumulus.form.Form
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.pane.Pane
import org.incendo.interfaces.kotlin.paper.asElement
import org.incendo.interfaces.kotlin.paper.asViewer
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement
import org.incendo.interfaces.paper.type.ChestInterface

abstract class NautilusInterface<T : Pane> {

    private val nautilus: Nautilus = Nautilus.get()
    private val floodgate = this.nautilus.floodgate
    var parent: NautilusInterface<*>? = null; private set

    abstract fun `interface`(): Interface<T, PlayerViewer>

    open fun form(viewer: Player): Form? = null

    open fun open(viewer: PlayerViewer) = `interface`().open(viewer)

    fun open(viewer: Player) {
        val floodgatePlayer = this.floodgate?.getPlayer(viewer.uniqueId)
        val form = if (floodgatePlayer != null) this.form(viewer) else null

        if (form != null) {
            floodgatePlayer!!.sendForm(form)
            return
        }

        this.open(viewer.asViewer())
    }

    fun parent(parent: NautilusInterface<*>): NautilusInterface<T> {
        this.parent = parent
        return this
    }

    open fun returnIcon(): ItemStackElement<T>? {
        val parent = this.parent ?: return null
        val face = parent.`interface`()
        val title = if (face is ChestInterface) face.title() else text("Back")

        return ItemBuilder(Material.ARROW)
            .name(text("← ", color(251, 228, 96)).append(title))
            .build()
            .asElement {
                it.viewer().player().playClickSound()
                parent.open(it.viewer())
            }
    }
}