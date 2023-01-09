package net.tjalp.nautilus.clan

import io.ktor.http.*
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
import org.litote.kmongo.regex
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

    /**
     * Get a [ClanSnapshot] based on its id. This method
     * will return the cached clan if it exists.
     *
     * @param id The id of the [ClanSnapshot]
     * @return The [ClanSnapshot] with the id, or null if nonexistent
     */
    suspend fun clan(id: ObjectId): ClanSnapshot? {
        if (clanCache.containsKey(id)) return clanCache[id]

        return this.clans.findOneById(id)
    }

    /**
     * Get a [ClanSnapshot] based on its name. This method
     * will return the cached clan if it exists.
     *
     * @param name The name of the [ClanSnapshot]
     * @return The (first) [ClanSnapshot] with the name, or null if nonexistent
     */
    suspend fun clan(name: String): ClanSnapshot? {
        clanIfCached(name)?.let { return it }

        return this.clans.find(
            ClanSnapshot::name regex Regex("^${name.escapeIfNeeded()}$", RegexOption.IGNORE_CASE)
        ).first()
    }

    /**
     * Get a [ClanSnapshot] from a profile. This method
     * will return the cached clan if it exists.
     *
     * @param profile The profile to get the clan of
     * @return The [ClanSnapshot] the profile has, or null if nonexistent
     */
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

    /**
     * Retrieve the latest **cached** [ClanSnapshot] by
     * the name of it
     *
     * @param name The name to search for
     * @return The clan if cached, otherwise null
     */
    fun clanIfCached(name: String): ClanSnapshot? {
        return this.clanCache.values.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    /**
     * Create a clan with a single leader. This method
     * will make the clan, then return it once created.
     *
     * @param leader The leader of the clan to create
     * @param name The name the clan should have
     */
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

    /**
     * Disband an existing clan meaning all players
     * will leave the clan, including the leaders.
     * The clan will be deleted afterwards.
     *
     * @param clan The clan to disband.
     */
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
        this.uncacheClan(clan)
    }

    /**
     * Method to update the cache and call relevant
     * events when a profile has been updated.
     *
     * @param clan The clan that was 'updated'.
     */
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

    /**
     * Check the cache to see if a clan should
     * be cached (or not). It will then update
     * the cache.
     *
     * @param clan The clan to check the cached version of
     */
    private fun checkCache(clan: ClanSnapshot) {
        if (shouldCache(clan)) {
            cacheClan(clan)
            return
        }

        uncacheClan(clan)
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

    /**
     * Remove a clan from the cache
     *
     * @param clan The clan to uncache
     * @return The previous clan if exists, otherwise null
     */
    private fun uncacheClan(clan: ClanSnapshot): ClanSnapshot? {
        nautilus.logger.info("Removing cached clan named '${clan.name}' (${clan.id})")
        return this.clanCache.remove(clan.id)
    }

    /**
     * Whether a clan should be cached or not.
     *
     * @param clan The clan to check
     * @return Whether the clan should be cached
     */
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