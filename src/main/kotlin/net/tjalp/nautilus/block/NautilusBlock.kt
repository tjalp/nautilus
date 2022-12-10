package net.tjalp.nautilus.block

import org.bukkit.Instrument
import org.bukkit.Note
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

abstract class NautilusBlock {

    abstract val identifier: String

    abstract val instrument: Instrument

    abstract val note: Note

    abstract val customModelData: Int?

    open fun onRightClick(event: PlayerInteractEvent) {}

    open fun onLeftClick(event: PlayerInteractEvent) {}

    open fun onBreak(event: BlockBreakEvent) {}

    open fun onPlace(event: BlockPlaceEvent) {}
}