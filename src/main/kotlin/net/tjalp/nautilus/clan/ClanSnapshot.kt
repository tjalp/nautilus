package net.tjalp.nautilus.clan

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.world.claim.WorldChunkMap
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import org.litote.kmongo.json
import java.util.*

/**
 * A clan is a group of people that work together
 * and own land together.
 */
data class ClanSnapshot(
    @BsonId val id: ObjectId,
    val name: String = UUID.randomUUID().toString().take(8),
    val leaders: Set<UUID> = emptySet(),
    val members: Set<UUID> = emptySet(),
    val claimedChunks: Set<WorldChunkMap> = emptySet(),
    val theme: String? = null
) {

    private val nautilus = Nautilus.get()
    private val clansCollection = MongoCollections.clans
    private val clans = this.nautilus.clans

    /**
     * Updates a clan with a bson query.
     *
     * If no bson query is added, the latest clan
     * from the collection will be cached.
     *
     * @param bson The bson query
     * @return The updated clan
     */
    suspend fun update(vararg bson: Bson): ClanSnapshot {
        val updatedClan =
            if (bson.isEmpty()) {
                this.clansCollection.findOneById(this.id) ?: this
            } else {
                Bukkit.broadcastMessage(combine(*bson).json)
                this.clansCollection.findOneAndUpdate(
                    ::id eq this.id,
                    combine(*bson),
                    FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                ) ?: this
            }

        this.clans.onClanUpdate(updatedClan)
        return updatedClan
    }

    /**
     * Updates a clan
     */
    fun update(clan: ClanSnapshot): ClanSnapshot {
        this.clans.onClanUpdate(clan)
        return clan
    }

    /**
     * Get the theme color of a [ClanSnapshot]
     *
     * @return The theme color
     */
    fun theme(): TextColor {
        return this.theme?.let { TextColor.fromHexString(it) } ?: NamedTextColor.WHITE
    }

    /**
     * Get the maximum amount of chunks this clan is able to claim
     * based on the amount of members.
     *
     * @return The maximum amount of chunks this clan can claim
     */
    fun chunkLimit(): Int = this.leaders.plus(this.members).size * 5

    /**
     * Get the amount of chunks this clan has claimed
     *
     * @return The amount of chunks this clan has claimed
     */
    fun claimedChunksCount(): Int = this.claimedChunks.sumOf { it.chunks.size }
}
