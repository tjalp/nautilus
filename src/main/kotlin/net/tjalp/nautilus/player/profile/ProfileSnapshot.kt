package net.tjalp.nautilus.player.profile

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import kotlinx.coroutines.reactive.awaitSingle
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.player.profile.data.PermissionInfo
import net.tjalp.nautilus.registry.Skins
import net.tjalp.nautilus.util.SkinBlob
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * A profile snapshot is a snapshot of the current
 * profile of a user. This may not be accurate to
 * the current data, since it's only a snapshot.
 */
data class ProfileSnapshot(
    @BsonId val uniqueId: UUID,
    val data: String? = null,
    val lastKnownName: String = "(unknown)",
    val lastKnownSkin: SkinBlob = Skins.QUESTION_MARK,
    val lastOnline: LocalDateTime = LocalDateTime.now(),
    val firstJoin: LocalDateTime = LocalDateTime.now(),
    val maskName: String? = null,
    val maskRank: String? = null,
    val maskSkin: SkinBlob? = null,
    val disguise: String? = null,
    val permissionInfo: PermissionInfo = PermissionInfo()
) {

    private val nautilus = Nautilus.get()
    private val scheduler = this.nautilus.scheduler
    private val profiles = MongoCollections.profiles

//    var data: String? = data
//        set(value) { field = value; set(::data, value) }
//
//    var permissionInfo: PermissionInfo = permissionInfo
//        set(value) { field = value; set(::permissionInfo, value) }

//    /**
//     * Update a value to the database
//     *
//     * @param key The key to set
//     * @param value The value to set
//     */
//    private fun <T> set(key: KProperty<T?>, value: T?) {
//        this.scheduler.launch {
//            this@ProfileSnapshot.profiles.updateOneById(this@ProfileSnapshot.uniqueId, setValue(key, value)).awaitSingle()
//        }
//    }
//
//    fun <T> update(key: KMutableProperty<T>, value: T) {
//        val callable = key.setter
//        Nautilus.get().logger.info("Parameters are ${callable.parameters}")
//        callable.callBy(mapOf(
//            callable.parameters[0] to this,
//            callable.parameters[1] to value
//        ))
//        this.scheduler.launch {
//            profiles.updateOneById(uniqueId, key setTo value).awaitSingle()
//        }
//    }

    /**
     * Updates a profile with a bson query
     *
     * @param bson The bson query
     * @return The updated profile
     */
    suspend fun update(vararg bson: Bson): ProfileSnapshot {
        if (bson.isEmpty()) return this

        val newProfile = this.profiles.findOneAndUpdate(
            ::uniqueId eq this.uniqueId,
            combine(*bson),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSingle()
        this.nautilus.profiles.onProfileUpdate(newProfile)
        return newProfile
    }

//    /**
//     * Save this [ProfileSnapshot] to the database
//     */
//    suspend fun save() {
//        this.profiles.save(this).awaitSingle()
//    }
}