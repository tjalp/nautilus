package net.tjalp.nautilus.interfaces

import net.tjalp.nautilus.Nautilus
import org.bukkit.entity.Player
import org.geysermc.cumulus.form.Form
import org.geysermc.floodgate.api.player.FloodgatePlayer
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.pane.Pane
import org.incendo.interfaces.kotlin.paper.asViewer
import org.incendo.interfaces.paper.PlayerViewer

abstract class NautilusInterface<T : Pane> {

    private val nautilus: Nautilus = Nautilus.get()
    private val floodgate = this.nautilus.floodgate

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
}