package net.tjalp.nautilus.clan

import kotlinx.coroutines.runBlocking
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.event.ClanUpdateEvent
import net.tjalp.nautilus.event.ProfileLoadEvent
import net.tjalp.nautilus.event.ProfileUnloadEvent
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.clan
import net.tjalp.nautilus.util.register
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.litote.kmongo.`in`
import org.litote.kmongo.setTo
import java.util.*

/**
 * Manages everything that has to do with [ClanSnapshot]s.
 *
 * This includes caching, adding leaders/members, etc.
 */
class ClanManager(
    private val nautilus: Nautilus
) {

    private val clanCache = HashMap<ObjectId, ClanSnapshot>()

    private val clans = MongoCollections.clans

    init {
        ClanListener().register()
    }

    suspend fun clan(id: ObjectId): ClanSnapshot? {
        if (clanCache.containsKey(id)) return clanCache[id]

        return this.clans.findOneById(id)
    }

    suspend fun clan(profile: ProfileSnapshot): ClanSnapshot? {
        return if (profile.clanId == null) null else this.clan(profile.clanId)
    }

    /**
     * Retrieve the latest **cached** [ClanSnapshot] of an
     * object id.
     *
     * @param id The object id of the cached clan
     * @return The clan if cached, otherwise null
     */
    fun clanIfCached(id: ObjectId): ClanSnapshot? = this.clanCache[id]

    suspend fun createClan(leader: UUID, name: String): ClanSnapshot {
        val clan = ClanSnapshot(
            id = ObjectId(),
            name = name,
            leaders = setOf(leader)
        )

        this.clans.insertOne(clan)

        if (shouldCache(clan)) cacheClan(clan)

        return clan
    }

    suspend fun disbandClan(clan: ClanSnapshot) {
        val profiles = this.nautilus.profiles
        val uniqueIds = clan.leaders + clan.members

        MongoCollections.profiles.updateMany(
            ProfileSnapshot::uniqueId `in` uniqueIds,
            ProfileSnapshot::clanId setTo null
        )

        for (uniqueId in uniqueIds) {
            profiles.profileIfCached(uniqueId)?.update()
        }

        this.clans.deleteOneById(clan.id)
    }

    fun onClanUpdate(clan: ClanSnapshot) {
        var previous: ClanSnapshot? = null

        for (uniqueId in clan.leaders.plus(clan.members)) {
            if (this.nautilus.server.getPlayer(uniqueId)?.isOnline != true) continue
            previous = this.cacheClan(clan)
        }

        val event = ClanUpdateEvent(
            clan = clan,
            previous = previous
        )

        if (Bukkit.isPrimaryThread()) {
            event.callEvent()
            return
        }

        this.nautilus.server.scheduler.runTask(this.nautilus, Runnable {
            event.callEvent()
        })
    }

    private fun checkCache(clan: ClanSnapshot) {
        if (shouldCache(clan)) {
            cacheClan(clan)
            return
        }

        nautilus.logger.info("Removing cached clan named '${clan.name}' (${clan.id})")
        this.clanCache -= clan.id
    }

    /**
     * Cache a clan
     *
     * @param clan The clan to cache
     * @return The previous clan if exists, otherwise null
     */
    private fun cacheClan(clan: ClanSnapshot): ClanSnapshot? {
        nautilus.logger.info("Caching clan named '${clan.name}' (${clan.id})")
        return this.clanCache.put(clan.id, clan)
    }

    private fun shouldCache(clan: ClanSnapshot): Boolean {
        return clan.leaders.plus(clan.members).any { nautilus.server.getPlayer(it)?.isOnline == true }
    }

    private inner class ClanListener : Listener {

        @EventHandler
        fun on(event: ProfileLoadEvent) {
            val profile = event.profile

            if (profile.clanId == null) return

            runBlocking {
                val clan = clan(profile) ?: return@runBlocking

                cacheClan(clan)
            }
        }

        @EventHandler
        fun on(event: ProfileUnloadEvent) {
            val profile = event.profile
            val clan = profile.clan() ?: return

            if (shouldCache(clan)) return

            checkCache(clan)
        }

        @EventHandler
        fun on(event: ProfileUpdateEvent) {
            val profile = event.profile
            val prev = event.previous
            val clan = profile.clan() ?: return
            val prevClan = prev?.clan() ?: return

            if (clan == prevClan) return

            checkCache(clan)
        }
    }
}