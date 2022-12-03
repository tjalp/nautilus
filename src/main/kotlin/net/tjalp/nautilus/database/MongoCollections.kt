package net.tjalp.nautilus.database

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.player.linking.GoogleLinkToken
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import org.litote.kmongo.reactivestreams.getCollectionOfName

/**
 * Contains a list of every collection
 * that is available in the database.
 */
object MongoCollections {

    private val database = Nautilus.get().mongo.mongoDatabase

    /** The 'profiles' collection */
    val profiles = this.database.getCollectionOfName<ProfileSnapshot>("profiles")

    /** The 'googleToken' collection */
    val linkTokens = this.database.getCollectionOfName<GoogleLinkToken>("googleLinkTokens")
}