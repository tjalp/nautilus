package net.tjalp.nautilus.block

import com.comphenix.protocol.PacketType.Play.Server.*
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedBlockData
import com.jeff_media.customblockdata.CustomBlockData
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.item.NautilusItem
import net.tjalp.nautilus.registry.block.SecondTestBlock
import net.tjalp.nautilus.registry.block.TestBlock
import net.tjalp.nautilus.registry.item.BlockItem
import net.tjalp.nautilus.util.ItemBuilder
import net.tjalp.nautilus.util.register
import org.bukkit.GameMode.CREATIVE
import org.bukkit.Material
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

class BlockManager(
    private val nautilus: Nautilus
) {

    private val registeredBlocks = mutableSetOf<NautilusBlock>()
    private val blocksById = HashMap<String, NautilusBlock>()

    init {
        BlockListener().apply {
            register()
//            nautilus.protocol.addPacketListener(this)
        }

        registerBlock(TestBlock)
        registerBlock(SecondTestBlock)
    }

    /**
     * Register a [NautilusBlock]. This will also
     * register an [ItemStack] with the corresponding
     * data.
     *
     * @param block The item to register
     */
    fun registerBlock(block: NautilusBlock) {
        check(block.identifier !in this.blocksById) { "Nautilus block already registered" }
        check(!this.nautilus.items.itemExists(block.identifier)) { "Nautilus block ITEM already registered" }

        this.nautilus.items.registerItem(BlockItem(
            block.identifier,
            block.customModelData
        ))

        this.registeredBlocks += block
        this.blocksById[block.identifier] = block
    }

    /**
     * Get a [NautilusBlock] by the identifier
     *
     * @param id The identifier
     * @return This nautilus block
     */
    fun getBlock(id: String): NautilusBlock {
        return this.blocksById[id.lowercase()] ?: throw IllegalArgumentException("Nautilus block by id $id does not exist!")
    }

    /**
     * Get a [NautilusBlock] by the block.
     *
     * @param block The block to check.
     * @return The [NautilusBlock], or null if nonexistent.
     */
    fun getBlock(block: Block): NautilusBlock? {
        return this.getBlock(this.identifier(block) ?: return null)
    }

    /**
     * Get a [NautilusBlock] by the block data.
     *
     * @param block The block to check.
     * @return The [NautilusBlock], or null if nonexistent.
     * @deprecated Use [BlockManager.getBlock(Block)]
     */
    fun getBlock(block: BlockData): NautilusBlock? {
        return this.getBlock(this.identifier(block) ?: return null)
    }

    /**
     * Whether a [NautilusBlock] with the specified id
     * exists.
     *
     * @param id The identifier to check
     * @return true if exists, false otherwise
     */
    fun blockExists(id: String): Boolean {
        return id in this.blocksById
    }

    /**
     * Whether this item is this specific
     * nautilus item.
     *
     * @param block The [ItemStack] to check
     * @return Whether it is this nautilus item
     */
    fun isBlock(block: Block, nautilusBlock: NautilusBlock): Boolean {
        return this.identifier(block)?.contains(nautilusBlock.identifier) ?: false
    }

    fun identifier(block: Block): String?  {
        val data = CustomBlockData(block, this.nautilus)

        return data.get(NautilusBlock.NAUTILUS_BLOCK_ID_PDC, STRING)
    }

    /**
     * Get the identifier of an item stack, which
     * may be null.
     */
    fun identifier(block: BlockData): String? {
        if (block !is NoteBlock) return null

        return this.registeredBlocks.firstOrNull {
            it.instrument == block.instrument && it.note == block.note
        }?.identifier
    }

    /**
     * Get a set of all the [NautilusBlock]s that
     * are registered. The set is immutable.
     *
     * @return An immutable set of [NautilusBlock]s
     */
    fun blocks(): Set<NautilusBlock> = this.registeredBlocks.toSet()

    private inner class BlockListener : PacketAdapter(this.nautilus, MAP_CHUNK, BLOCK_CHANGE, MULTI_BLOCK_CHANGE), Listener {

        override fun onPacketSending(event: PacketEvent) {
            when (event.packetType) {
                MAP_CHUNK -> this.onMapChunk(event)
                BLOCK_CHANGE -> this.onBlockChange(event)
                MULTI_BLOCK_CHANGE -> this.onMultiBlockChange(event)
            }
        }

        private fun onMapChunk(event: PacketEvent) {
//            val packet = event.packet
//            val chunkData = packet.levelChunkData.read(0)
//            val handle = chunkData.handle as ClientboundLevelChunkPacketData
//
//            for (info in chunkData.blockEntityInfo) {
//                val x = info.sectionX.toDouble()
//                val y = info.y.toDouble()
//                val z = info.sectionZ.toDouble()
//                val location = Location(event.player.world, x, y, z)
//
//                if (location.block.blockData !is NoteBlock) continue
//
//                event.player.sendMessage("$x, $y, $z")
//            }
        }

        private fun onBlockChange(event: PacketEvent) {
            val packet = event.packet
            val block = packet.blockPositionModifier.read(0).toLocation(event.player.world).block
            val blockData = block.blockData

            if (blockData !is NoteBlock) return
            val ntlBlock = this@BlockManager.getBlock(block) ?: return

            blockData.note = ntlBlock.note
            blockData.instrument = ntlBlock.instrument

            packet.blockData.write(0, WrappedBlockData.createData(blockData))
        }

        private fun onMultiBlockChange(event: PacketEvent) {
            val packet = event.packet

            for (info in packet.multiBlockChangeInfoArrays.read(0)) {
                val block = info.getLocation(event.player.world).block
                val blockData = block.blockData

                if (blockData !is NoteBlock) return
                val ntlBlock = this@BlockManager.getBlock(block) ?: return

                blockData.note = ntlBlock.note
                blockData.instrument = ntlBlock.instrument

                info.data = WrappedBlockData.createData(blockData)
            }
        }

        @EventHandler
        fun on(event: PlayerInteractEvent) {
            val player = event.player
            val block = event.clickedBlock ?: return
            val data = block.blockData
            val ntlBlock = getBlock(block)

            if (data !is NoteBlock) return

            when (event.action) {
                RIGHT_CLICK_BLOCK -> {
                    if (player.isSneaking && player.inventory.itemInMainHand.type != Material.AIR) return

                    event.setUseInteractedBlock(Event.Result.DENY)
                    ntlBlock?.onRightClick(event)
                }
                LEFT_CLICK_BLOCK -> if (player.gameMode != CREATIVE) ntlBlock?.onLeftClick(event)
                else -> {}
            }
        }

        @EventHandler
        fun on(event: BlockDropItemEvent) {
            val ntlBlock = getBlock(event.block) ?: return
            val ntlItem = nautilus.items.getItem(ntlBlock.identifier)

            for (item in event.items.filter { it.itemStack.type == NOTE_BLOCK }) {
                ItemBuilder(item.itemStack)
                    .customModelData(ntlItem.customModelData)
                    .data(NautilusItem.NAUTILUS_ITEM_ID_PDC, ntlItem.identifier)
                    .build()
            }
        }

        @EventHandler
        fun on(event: BlockBreakEvent) {
            getBlock(event.block)?.onBreak(event)
        }

        @EventHandler
        fun on(event: BlockPlaceEvent) {
            val ntlItem = nautilus.items.getItem(event.itemInHand).firstOrNull() ?: return

            if (!blockExists(ntlItem.identifier)) return

            val ntlBlock = getBlock(ntlItem.identifier)
            val data = event.blockPlaced.blockData
            val pdc = CustomBlockData(event.blockPlaced, nautilus)

            if (data !is NoteBlock) return

            data.note = ntlBlock.note
            data.instrument = ntlBlock.instrument
            event.blockPlaced.blockData = data
            pdc.set(NautilusBlock.NAUTILUS_BLOCK_ID_PDC, STRING, ntlBlock.identifier)
            ntlBlock.onPlace(event)
        }

        @EventHandler
        fun on(event: BlockPhysicsEvent) {
            if (event.sourceBlock.blockData is NoteBlock) {
                event.isCancelled = true
                return
            }

            val data = event.changedBlockData

            if (data is NoteBlock) {
                event.isCancelled = true

                nautilus.server.scheduler.runTask(nautilus, Runnable {
                    // I absolutely hate this
                    val newBlock = event.block.location.block

                    if (newBlock.blockData !is NoteBlock) return@Runnable

                    // NOT THIS :(((((
                    newBlock.setBlockData(data, false)
                })
            } // todo fix this??? idk
        }

        @EventHandler
        fun on(event: NotePlayEvent) {
            event.isCancelled = true
        }
    }
}