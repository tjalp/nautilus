package net.tjalp.nautilus.registry.block

import net.tjalp.nautilus.block.NautilusBlock
import org.bukkit.Instrument
import org.bukkit.Note
import org.bukkit.event.player.PlayerInteractEvent

object TestBlock : NautilusBlock() {

    override val identifier = "test-block"
    override val instrument = Instrument.PIANO
    override val note = Note(0)
    override val customModelData = null

    override fun onRightClick(event: PlayerInteractEvent) {
        super.onRightClick(event)

        event.player.sendMessage("Test block!")
    }

    override fun onLeftClick(event: PlayerInteractEvent) {
        super.onLeftClick(event)

        event.player.sendMessage("Left clicked!")
    }
}