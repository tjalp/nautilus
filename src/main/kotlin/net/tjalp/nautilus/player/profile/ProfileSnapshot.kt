package net.tjalp.nautilus.player.profile

import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

/**
 * A profile snapshot is a snapshot of the current
 * profile of a user. This may not be accurate to
 * the current data, since it's only a snapshot.
 */
data class ProfileSnapshot(
    @BsonId val uniqueId: UUID,
    var data: String = ""
)