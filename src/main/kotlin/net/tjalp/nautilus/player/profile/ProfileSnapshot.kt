package net.tjalp.nautilus.player.profile

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import kotlinx.coroutines.reactive.awaitSingle
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.player.profile.data.PermissionInfo
import net.tjalp.nautilus.util.SkinBlob
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
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
    val lastKnownSkin: SkinBlob = SkinBlob("ewogICJ0aW1lc3RhbXAiIDogMTY2NTY4MTM0ODA2NywKICAicHJvZmlsZUlkIiA6ICIwNDNkZWIzOGIyNjE0MTg1YTIzYzU4ZmI2YTc5ZWZkYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJWaXRhbFNpZ256MiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yOWIzN2ZkMDE0MjIzYTk5NmQxYmJkMGY0NmQwN2M2YzViMDlmM2IxMTRhNTgyMWQ2N2RlYTE4YTY2YmY5ZDRlIgogICAgfQogIH0KfQ==", "erK6MuC6f5PMf3vUcmwCrOtcgvSvTfj4vgd4qkrNgPMnR5tp0EEiBKDgb1t5Bou9UUlZcJqxzfVomNAbkkD13P3yVxbcw/rENK6lwCtAPJMG3miM/mGNS9CTpFd2aGPcDajaH9dztdxgdNMFS3f088kOUxV1a7NT0hicSBb8ZxDPcGgQkKlpH1hI88uqC4kV4N0CeaSlb3K0du1Q9Bgd5bmDJ4Ns5goOQtOhIPHkWwOt/s0NgIYTS7Glo2YxG4Kk5WvaN4dxzpZTBtLv95+zrR2Rok/720WKG/00A5HkcDO2hoM9ewHVPSUPmgRZS9wrQqPTEOut2xZ2Qac1wfsVKYIpQpI+DyOwlCAiTeRP1YGf/emcvTdM37FdLMZC4DntKZIdV7WExRYQS9DLZRoFA1//HaiBi+QU5iRWMEBdFfyYdLye1nNGOgrXTNRQi8Aj+9Ky9j5PwXIUVMC4ehOQolqUGZ17az9j0HYzZYMiKbNkGf5TxRevp3O3VRMar6ECJ1RlZ0rm4yc1IUlWfz+D9k/rXvGa6wt4hXPiPkCJwP58ua9wRHwbPzb36RddBffR3uv5h71FfDrn0L7QX7k6ePMhWx11duvlWDAJ7z2ULG6jNmBJCPW2/KniFlIjgfAmC94grZZSdAv7PS3ac3osn6hw2pTUoTF+chE9Uo37MQI="),
    val lastOnline: LocalDateTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
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
        val newProfile = this.profiles.findOneAndUpdate(
            ::uniqueId eq this.uniqueId,
            bson.toList(),
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