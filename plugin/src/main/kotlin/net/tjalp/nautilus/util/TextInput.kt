package net.tjalp.nautilus.util

import com.comphenix.protocol.PacketType.Play.Client.UPDATE_SIGN
import com.comphenix.protocol.PacketType.Play.Server.OPEN_SIGN_EDITOR
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket
import net.tjalp.nautilus.Nautilus
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.function.Consumer

object TextInput : Listener {

    private val protocol: ProtocolManager
    private val signMapping = mutableMapOf<UUID, Consumer<Component>>()

    init {
        val nautilus = Nautilus.get()
        this.protocol = nautilus.protocol

        this.register()

        protocol.addPacketListener(object : PacketAdapter(nautilus, UPDATE_SIGN) {

            override fun onPacketReceiving(event: PacketEvent) {
                val packet = event.packet
                val player = event.player
                val lines = packet.stringArrays.read(0).map { text(it) }
                val pos = packet.blockPositionModifier.read(0).toLocation(player.world)

                signMapping[player.uniqueId]?.run {
                    nautilus.server.scheduler.runTask(nautilus, Runnable {
                        accept(lines[0])
                    })
                }

                player.sendBlockChange(pos, pos.block.blockData)
            }
        })
    }

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        signMapping.remove(event.player.uniqueId)?.accept(empty())
    }

    fun signSmall(player: Player, label: Component = empty(), default: Component = empty(), consumer: Consumer<Component>) {
        setSign(player, listOf(default, text("^^^"), label, empty()), consumer)
    }

    private fun setSign(player: Player, lines: List<Component>, consumer: Consumer<Component> ) {
        val loc = player.location.apply { y -= 6 }
        val blockData = Material.OAK_WALL_SIGN.createBlockData()
        val blockState = blockData.createBlockState() as Sign
        val signSide = blockState.getSide(Side.FRONT)

        lines.forEachIndexed { index, component ->
            signSide.line(index, component)
        }

        player.closeInventory()
        player.sendBlockChange(loc, blockData)
        player.sendBlockUpdate(loc, blockState)
//        (player as CraftPlayer).handle.connection.send(ClientboundOpenSignEditorPacket(BlockPos(loc.blockX, loc.blockY, loc.blockZ), true))
        openSign(player, loc)

        signMapping[player.uniqueId] = consumer
    }

    private fun openSign(player: Player, location: Location) {
        val packet = PacketContainer(OPEN_SIGN_EDITOR)
        val pos = BlockPosition(location.toVector())

        packet.modifier.writeDefaults()
        packet.blockPositionModifier.write(0, pos)
        packet.booleans.write(0, true)

        protocol.sendServerPacket(player, packet)
    }
}