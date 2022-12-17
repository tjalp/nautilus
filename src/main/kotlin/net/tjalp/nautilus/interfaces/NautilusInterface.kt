package net.tjalp.nautilus.interfaces

import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.pane.Pane
import org.incendo.interfaces.paper.PlayerViewer

abstract class NautilusInterface<T : Pane> {

    abstract fun `interface`(): Interface<T, PlayerViewer>

    fun open(viewer: PlayerViewer) = `interface`().open(viewer)
}