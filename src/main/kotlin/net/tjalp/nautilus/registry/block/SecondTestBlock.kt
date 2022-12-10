package net.tjalp.nautilus.registry.block

import net.tjalp.nautilus.block.NautilusBlock
import org.bukkit.Instrument
import org.bukkit.Note
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

object SecondTestBlock : NautilusBlock() {

    override val identifier = "second-test-block"
    override val instrument = Instrument.PIANO
    override val note = Note(1)
    override val customModelData = null

    override fun onRightClick(event: PlayerInteractEvent) {
        super.onRightClick(event)

        event.player.sendMessage("Second Test block!")
    }

    override fun onLeftClick(event: PlayerInteractEvent) {
        super.onLeftClick(event)

        event.player.sendMessage("Left clicked second test block!")
    }

    override fun onBreak(event: BlockBreakEvent) {
        super.onBreak(event)

        event.player.sendMessage("Broke the second test block!")
    }

    override fun onPlace(event: BlockPlaceEvent) {
        super.onPlace(event)

        event.player.sendMessage("Placed the second test block!")
    }
}