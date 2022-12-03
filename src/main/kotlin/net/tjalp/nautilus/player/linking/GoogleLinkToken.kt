package net.tjalp.nautilus.player.linking

import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

/**
 * A Google Link Token that represents a unique identifier
 * with a special, randomly generated token tied to it.
 */
data class GoogleLinkToken(
    @BsonId val uniqueId: UUID,
    val token: String? = null
)