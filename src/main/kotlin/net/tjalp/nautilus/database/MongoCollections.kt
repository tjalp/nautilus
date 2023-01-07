package net.tjalp.nautilus.database

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.clan.ClanSnapshot
import net.tjalp.nautilus.player.linking.GoogleLinkToken
import net.tjalp.nautilus.player.profile.GoogleUser
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import org.litote.kmongo.reactivestreams.getCollectionOfName

/**
 * Contains a list of every collection
 * that is available in the database.
 */
object MongoCollections {

    private val database = Nautilus.get().mongo.mongoDatabase

    /** The 'profiles' collection */
    val profiles = this.database.getCollection<ProfileSnapshot>("profiles")

    /** The 'googleToken' collection */
    val linkTokens = this.database.getCollection<GoogleLinkToken>("googleLinkTokens")

    /** The 'users' collection */
    val googleUsers = this.database.getCollection<GoogleUser>("users")

    /** The 'clans' collection */
    val clans = this.database.getCollection<ClanSnapshot>("clans")
}