package net.tjalp.nautilus.player.profile

import kotlinx.coroutines.reactive.awaitSingle
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.player.profile.data.InformationObject
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.reactivestreams.save
import java.util.*

/**
 * A profile snapshot is a snapshot of the current
 * profile of a user. This may not be accurate to
 * the current data, since it's only a snapshot.
 */
data class ProfileSnapshot(
    @BsonId val uniqueId: UUID,
    var data: String? = null,
    val info: InformationObject = InformationObject("random", 16)
) {

    private val profiles = MongoCollections.profiles

    /**
     * Save this [ProfileSnapshot] to the database
     */
    suspend fun save() {
        this.profiles.save(this).awaitSingle()
    }
}