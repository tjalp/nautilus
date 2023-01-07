package net.tjalp.nautilus.clan

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import kotlinx.coroutines.reactive.awaitSingle
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.combine
import org.litote.kmongo.eq
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
    val chunks: Set<Long> = emptySet()
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
                this.clansCollection.findOneAndUpdate(
                    ::id eq this.id,
                    combine(*bson),
                    FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                ) ?: this
            }

        this.nautilus.clans.onClanUpdate(updatedClan)
        return updatedClan
    }
}
