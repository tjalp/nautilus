package net.tjalp.nautilus.world.claim

import com.jeff_media.morepersistentdatatypes.DataType
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.clan.ClanSnapshot
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.util.profile
import net.tjalp.nautilus.util.register
import org.bson.types.ObjectId
import org.bukkit.Chunk
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.persistence.PersistentDataType.LONG_ARRAY
import org.bukkit.scheduler.BukkitRunnable
import org.litote.kmongo.*
import java.time.Duration
import java.util.*
import kotlin.collections.set

/**
 * The claim manager manages everything
 * that has to do with land claiming.
 */
class ClaimManager(
    private val nautilus: Nautilus
) {

    init {
        ClaimListener().register()
    }

    /**
     * Claim a chunk for a specific unique identifier (player).
     *
     * @param player The claimer's unique id
     * @param chunk The chunk to claim
     * @return true when successful, false otherwise
     */
    @Deprecated(
        message = "Claiming is now done through clans",
        replaceWith = ReplaceWith("claim(ClanSnapshot, Chunk)"),
        level = DeprecationLevel.WARNING
    )
    fun claim(player: Player, chunk: Chunk): Boolean {
        val pdc = chunk.persistentDataContainer
        val playerPdc = player.persistentDataContainer

        if (this.hasOwner(chunk)) return false

        val worldChunkMap = mutableMapOf<UUID, LongArray>()
        val chunks = chunks(player)

        for (world in chunks.map { it.world }) {
            val chunkIds = chunks
                .filter { it.world == world }
                .map { it.chunkKey }
                .toLongArray()

            worldChunkMap[world.uid] = chunkIds
        }

        var array = worldChunkMap[chunk.world.uid] ?: longArrayOf()
        array += chunk.chunkKey

        worldChunkMap[chunk.world.uid] = array

        pdc.set(CHUNK_OWNER_PDC, DataType.UUID, player.uniqueId)
        playerPdc.set(OWNED_CHUNKS_PDC, DataType.asMap(DataType.UUID, LONG_ARRAY), worldChunkMap)
        return true
    }

    /**
     * Claim a chunk for a specific clan
     *
     * @param clan The clan to claim the chunk for
     * @param chunk The chunk to claim
     * @return true when successful, false otherwise
     */
    suspend fun claim(clan: ClanSnapshot, chunk: Chunk): Boolean {
        val chunkPdc = chunk.persistentDataContainer

        if (this.hasOwner(chunk)) return false

        chunkPdc.set(CHUNK_OWNER_PDC, DataType.STRING, clan.id.toHexString())

        val clans = MongoCollections.clans
        var updatedClan = clans.findOneAndUpdate(
            and(ClanSnapshot::id eq clan.id, ClanSnapshot::claimedChunks / WorldChunkMap::world eq chunk.world.uid),
            addToSet(
                ClanSnapshot::claimedChunks.posOp / WorldChunkMap::chunks,
                chunk.chunkKey
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )

        if (updatedClan == null) {
            updatedClan = clans.findOneAndUpdate(
                ClanSnapshot::id eq clan.id,
                addToSet(
                    ClanSnapshot::claimedChunks,
                    WorldChunkMap(chunk.world.uid, setOf(chunk.chunkKey))
                ),
                FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
            )
        }

        updatedClan?.let { clan.update(it) } ?: clan.update()
        return true
    }

    /**
     * Unclaim a chunk for a specific clan
     *
     * @param clan The clan to unclaim the chunk for
     * @param chunk The chunk to unclaim
     * @return true when successful, false otherwise
     */
    suspend fun unclaim(clan: ClanSnapshot, chunk: Chunk, updateClan: Boolean = true) {
        val chunkPdc = chunk.persistentDataContainer

        if (this.owner(chunk) != clan.id) {
            this.nautilus.logger.warning(
                "Found wrong/non-existent clan owner for chunk ${chunk.chunkKey} (x: ${chunk.x}, y: ${chunk.z}, world: ${chunk.world.name}) " +
                        "(expected ${clan.id.toHexString()}, found ${this.owner(chunk)})!"
            )
            return
        }

        chunkPdc.remove(CHUNK_OWNER_PDC)

        if (!updateClan) return

        val clans = MongoCollections.clans
        val updatedClan = clans.findOneAndUpdate(
            and(ClanSnapshot::id eq clan.id, ClanSnapshot::claimedChunks / WorldChunkMap::world eq chunk.world.uid),
            pull(
                ClanSnapshot::claimedChunks.posOp / WorldChunkMap::chunks,
                chunk.chunkKey
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )

        updatedClan?.let { clan.update(it) } ?: clan.update()
    }

    /**
     * Unclaim a chunk
     *
     * @param chunk The chunk to unclaim
     */
    suspend fun unclaim(chunk: Chunk, updateClan: Boolean = true) {
        val chunkPdc = chunk.persistentDataContainer
        val owner = this.owner(chunk)

        chunkPdc.remove(CHUNK_OWNER_PDC)

        if (!updateClan || owner == null) return

        val clans = MongoCollections.clans
        val clan = clans.findOneAndUpdate(
            and(ClanSnapshot::id eq owner, ClanSnapshot::claimedChunks / WorldChunkMap::world eq chunk.world.uid),
            pull(
                ClanSnapshot::claimedChunks.posOp / WorldChunkMap::chunks,
                chunk.chunkKey
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )

        clan?.let { clan.update(it) }
    }

    /**
     * Unclaim all chunks of a clan
     *
     * @param clan The clan to unclaim all chunks for
     */
    suspend fun unclaimAll(clan: ClanSnapshot) {
        val chunks = this.chunks(clan)

        chunks.forEachIndexed { index, chunk ->
            this.unclaim(clan, chunk, updateClan = false)
        }

        val clans = MongoCollections.clans
        val updatedClan = clans.findOneAndUpdate(
            and(ClanSnapshot::id eq clan.id),
            set(ClanSnapshot::claimedChunks setTo emptySet()),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )

        updatedClan?.let { clan.update(it) } ?: clan.update()
    }

    /**
     * Gets the owner of a chunk
     *
     * @param chunk The chunk to get the owner of
     * @return The unique id of the owner, or null if nonexistent
     */
    fun owner(chunk: Chunk): ObjectId? {
        return ObjectId(chunk.persistentDataContainer.get(CHUNK_OWNER_PDC, DataType.STRING) ?: return null)
    }

    /**
     * Gets whether a chunk has an owner
     *
     * @param chunk The chunk to check
     * @return true if the chunk has an owner, false otherwise
     */
    fun hasOwner(chunk: Chunk): Boolean = chunk.persistentDataContainer.has(CHUNK_OWNER_PDC)

    /**
     * Gets all owned chunks of a player
     */
    @Deprecated(
        message = "Claiming is now done through clans",
        replaceWith = ReplaceWith("claim(ClanSnapshot, Chunk)"),
        level = DeprecationLevel.WARNING
    )
    fun chunks(player: Player): Set<Chunk> {
        val pdc = player.persistentDataContainer
        val worldIds = pdc.get(OWNED_CHUNKS_PDC, DataType.asMap(DataType.UUID, LONG_ARRAY)) ?: emptyMap()
        val chunks = mutableSetOf<Chunk>()

        for (id in worldIds) {
            val world = this.nautilus.server.getWorld(id.key)

            if (world == null) {
                this.nautilus.logger.warning("Received world entry ${id.key}, but no such world exists! (player: ${player.name}, uniqueId: ${player.uniqueId})")
                continue
            }

            for (long in id.value) chunks += world.getChunkAt(long)
        }

        return chunks
    }

    /**
     * Gets all owned chunks of a clan
     *
     * @param clan The clan to get the chunks of
     * @return The chunks of the clan
     */
    fun chunks(clan: ClanSnapshot): Set<Chunk> {
        val chunks = mutableSetOf<Chunk>()

        for (map in clan.claimedChunks) {
            val world = this.nautilus.server.getWorld(map.world)

            if (world == null) {
                this.nautilus.logger.warning("Received world entry ${map.world}, but no such world exists! (clan: ${clan.name}, id: ${clan.id})")
                continue
            }

            for (long in map.chunks) chunks += world.getChunkAt(long)
        }

        return chunks
    }

    companion object {

        val CHUNK_OWNER_PDC = NamespacedKey("nautilus", "chunk_owner")

        val OWNED_CHUNKS_PDC = NamespacedKey("nautilus","owned_chunks")
    }

    private inner class ClaimListener : Listener {

        private val tasks = mutableSetOf<BukkitRunnable>()

        @EventHandler
        fun on(event: PluginDisableEvent) {
            if (event.plugin != nautilus) return

            this.tasks.iterator().forEach {
                it.cancel()
                it.run()
            }
        }

        @EventHandler
        fun on(event: PlayerMoveEvent) {
            val player = event.player
            val from = event.from.chunk
            val to = event.to.chunk
            val toOwner = owner(to)
            val fromOwner = owner(from)

            if (from == to) return
            if (fromOwner == toOwner) return

            if (toOwner != null) {
                this@ClaimManager.nautilus.scheduler.launch {
                    val clan = nautilus.clans.clan(toOwner) ?: return@launch
                    val title = title(
                        empty(),
                        text("Now entering", color(119, 221, 119), ITALIC)
                            .appendSpace().append(text(clan.name, clan.theme())
                                .decoration(ITALIC, false))
                            .append(text("'s territory")),
                        times(Duration.ofMillis(250), Duration.ofMillis(1500), Duration.ofMillis(500))
                    )

                    player.showTitle(title)
                }
            }

            if (toOwner == null && fromOwner != null) {
                this@ClaimManager.nautilus.scheduler.launch {
                    val clan = nautilus.clans.clan(fromOwner) ?: return@launch
                    val title = title(
                        empty(),
                        text("Now leaving", color(255,109,106), ITALIC)
                            .appendSpace().append(text(clan.name, clan.theme())
                                .decoration(ITALIC, false))
                            .append(text("'s territory")),
                        times(Duration.ofMillis(250), Duration.ofMillis(1500), Duration.ofMillis(500))
                    )

                    player.showTitle(title)
                }
            }
        }

        @EventHandler
        fun on(event: BlockBreakEvent) {
            val block = event.block
            val player = event.player

            if (!hasOwner(block.chunk)) return
            if (owner(block.chunk) == player.profile().clanId) return

            if (block.state is Container) {
                event.isCancelled = true
                return
            }
            event.isCancelled = true

            val type = block.type
            val location = block.location
            val world = block.world
            val data = block.blockData

            block.setType(Material.AIR, false)
            player.damageItemStack(player.inventory.itemInMainHand, 1)

            val task = object : BukkitRunnable() {
                override fun run() {
                    block.setBlockData(data, false)
                    world.playEffect(location, Effect.STEP_SOUND, type)
                    tasks -= this
                }
            }

            this.tasks += task
            task.runTaskLater(nautilus, 5 * 20)
        }

        @EventHandler
        fun on(event: BlockPlaceEvent) {
            val block = event.blockPlaced

            if (!hasOwner(block.chunk)) return
            if (owner(block.chunk) == event.player.profile().clanId) return

            event.isCancelled = true
        }

        @EventHandler
        fun on(event: PlayerInteractEvent) {
            val block = event.clickedBlock?.getRelative(event.blockFace) ?: return

            if (event.action != Action.RIGHT_CLICK_BLOCK) return
            if (block.state is Container) return
            if (!hasOwner(block.chunk)) return
            if (owner(block.chunk) == event.player.profile().clanId) return

            event.setUseInteractedBlock(Event.Result.DENY)
        }

        @EventHandler
        fun on(event: PlayerBucketEmptyEvent) {
            val block = event.block

            if (!hasOwner(block.chunk)) return
            if (owner(block.chunk) == event.player.profile().clanId) return

            event.isCancelled = true
        }
    }
}