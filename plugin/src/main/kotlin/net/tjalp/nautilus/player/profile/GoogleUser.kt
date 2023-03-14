package net.tjalp.nautilus.player.profile

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.util.*

/**
 * A Google user that is linked to a Minecraft account
 */
data class GoogleUser(
    @BsonId val id: ObjectId,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val googleId: String? = null,
    val canChangeMinecraftAccount: LocalDateTime? = null,
    val minecraftUuid: UUID? = null,
    val discordUserEntry: ObjectId? = null,
    val canChangeDiscordAccount: LocalDateTime? = null
)
