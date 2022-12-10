package net.tjalp.nautilus.block

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
     * register an itemstack with the corresponding
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
     * Get a [NautilusBlock] by the item.
     *
     * @param block The item to check.
     * @return The [NautilusBlock], or null if nonexistent.
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
    fun isBlock(block: BlockData, nautilusBlock: NautilusBlock): Boolean {
        return this.identifier(block)?.contains(nautilusBlock.identifier) ?: false
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

    private inner class BlockListener : Listener {

        @EventHandler
        fun on(event: PlayerInteractEvent) {
            val player = event.player
            val block = event.clickedBlock ?: return
            val data = block.blockData
            val ntlBlock = getBlock(data)

            if (data !is NoteBlock) return

            when (event.action) {
                RIGHT_CLICK_BLOCK -> {
                    if (!player.isSneaking || player.inventory.itemInMainHand.type == Material.AIR) event.setUseInteractedBlock(Event.Result.DENY) // todo allow placing of blocks against it
                    ntlBlock?.onRightClick(event)
                }
                LEFT_CLICK_BLOCK -> if (player.gameMode != CREATIVE) ntlBlock?.onLeftClick(event)
                else -> {}
            }
        }

        @EventHandler
        fun on(event: BlockDropItemEvent) {
            val ntlBlock = getBlock(event.blockState.blockData) ?: return
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
            getBlock(event.block.blockData)?.onBreak(event)
        }

        @EventHandler
        fun on(event: BlockPlaceEvent) {
            val ntlItem = nautilus.items.getItem(event.itemInHand).firstOrNull() ?: return

            if (!blockExists(ntlItem.identifier)) return

            val ntlBlock = getBlock(ntlItem.identifier)
            val data = event.blockPlaced.blockData

            if (data !is NoteBlock) return

            data.note = ntlBlock.note
            data.instrument = ntlBlock.instrument
            event.blockPlaced.blockData = data
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