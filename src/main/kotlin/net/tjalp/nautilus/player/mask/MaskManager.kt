package net.tjalp.nautilus.player.mask

import com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.comphenix.protocol.wrappers.WrappedSignedProperty
import com.destroystokyo.paper.profile.PlayerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.event.ProfileUpdateEvent
import net.tjalp.nautilus.permission.PermissionRank
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.*
import org.bson.conversions.Bson
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.litote.kmongo.setValue
import kotlin.system.measureTimeMillis

/**
 * Manages everything that has to do with masking,
 * such as skin changing, name changing, rank changing etc.
 */
class MaskManager(
    private val nautilus: Nautilus
) {

    private val perms = this.nautilus.perms

    init {
        MaskListener().apply {
            register()
//            this@MaskManager.nautilus.protocol.addPacketListener(this)
        }
    }

    /**
     * Mask a player/profile.
     *
     * @param profile The profile to mask
     * @param username The username to mask the profile to, may be null
     * @param rank The rank to mask the profile to, may be null
     * @param skin The skin to mask the profile to, may be null
     * @param message Whether a message should be sent to the player that owns this profile
     */
    suspend fun mask(
        profile: ProfileSnapshot,
        username: String? = null,
        rank: PermissionRank? = null,
        skin: String? = null,
        message: Boolean = true
    ) {
        val bson = arrayListOf<Bson>()

        if (username != null) bson += setValue(ProfileSnapshot::maskName, username)
        if (rank != null) bson += setValue(ProfileSnapshot::maskRank, rank.id)

        val updatedProfile: ProfileSnapshot
        var skinProfile: PlayerProfile? = null
        val time = measureTimeMillis {
            if (skin != null) {
                skinProfile = nautilus.server.createProfile(skin)
                val completed: Boolean

                withContext(Dispatchers.Default) {
                    completed = skinProfile!!.complete()
                }

                if (!completed) {
                    if (message) profile.player()?.sendMessage(mini("<red>Setting skin failed. Is there no user with that name?"))
                    return
                } else {
                    val texturesProperty = skinProfile!!.properties.first { it.name == "textures" }
                    bson += setValue(ProfileSnapshot::maskSkin, SkinBlob(texturesProperty.value, texturesProperty.signature!!))
                }
            }

            updatedProfile = profile.update(*bson.toTypedArray())
        }

        if (message) {
            if (username != null || rank != null) {
                profile.player()?.sendMessage(
                    text("You are now known as ", GRAY)
                        .append(updatedProfile.nameComponent(showSuffix = false))
                        .append(text(" (${time}ms)"))
                )
            } else {
                profile.player()?.sendMessage(
                    mini("<gray>Your skin has been set to <white>${skinProfile?.name ?: "(unknown)"}</white> (${time}ms)")
                )
            }
        }
    }

    /**
     * Gets the skin mask of the [ProfileSnapshot]
     *
     * @param profile The profile to get the skin mask of
     * @return The skin username/id
     */
    fun skin(profile: ProfileSnapshot): SkinBlob? = profile.maskSkin

    /**
     * Gets the rank mask of the [ProfileSnapshot]
     *
     * @param profile The profile to get the rank mask of
     * @return The rank in [PermissionRank] form
     */
    fun rank(profile: ProfileSnapshot): PermissionRank? {
        val rank = profile.maskRank

        if (rank == null || !this.perms.rankExists(rank)) return null

        return this.perms.getRank(rank)
    }

    /**
     * Gets the username mask of the [ProfileSnapshot]
     *
     * @param profile The profile to get the username mask of
     * @return The username
     */
    fun username(profile: ProfileSnapshot): String? = profile.maskName

    /**
     * Generate a [WrappedGameProfile] of a player
     *
     * @param profile The profile to get the values from
     * @param identity The original gameprofile, may be null
     * @return The new [WrappedGameProfile]
     */
    fun generateGameProfile(profile: ProfileSnapshot, identity: WrappedGameProfile? = null): WrappedGameProfile? {
//        val username = profile.displayName()
        val username = profile.player()?.name ?: profile.lastKnownName
        val uniqueId = profile.uniqueId
        val gameProfile = identity ?: WrappedGameProfile(uniqueId, username)
        val skinBlob = if (profile.maskSkin != null) {
            nautilus.logger.info("Has mask skin")
            profile.maskSkin
        } else {
            nautilus.logger.info("Does not have maskSkin")
            if (identity == null) return null
            nautilus.logger.info("Identity is there")

            val texturesProperty = identity.properties.get("textures").firstOrNull() ?: return null
            nautilus.logger.info("textures exists")

            SkinBlob(
                texturesProperty.value,
                texturesProperty.signature
            )
        }

        gameProfile.properties.put("textures", WrappedSignedProperty.fromValues(
            "textures",
            skinBlob.value,
            skinBlob.signature
        ))

        return gameProfile
    }

    /**
     * The mask manager listener to do everything
     * that has to do with masking.
     */
    private inner class MaskListener : Listener, PacketAdapter(nautilus, PLAYER_INFO) {

        override fun onPacketSending(event: PacketEvent) {
            when (event.packetType) {
                PLAYER_INFO -> onPlayerInfo(event)
            }
        }

        private fun onPlayerInfo(event: PacketEvent) {
            nautilus.logger.info("Packet event fired!")
            val packet = event.packet
            val playerInfoData = packet.playerInfoDataLists.read(0).map { info ->
                val player = nautilus.server.getPlayer(info.profile.uuid) ?: return@map info
                val profile = player.profile()
                nautilus.logger.info("Player info data map")

                if (event.player == player) return@map info

                if (
                    profile.maskName == null
                    && profile.maskRank == null
                    && profile.maskSkin == null
                ) return@map info

                nautilus.logger.info("Has a mask")

                return@map PlayerInfoData(
                    generateGameProfile(profile, info.profile),
                    info.latency,
                    info.gameMode,
                    info.displayName
                )
            }

            packet.playerInfoDataLists.write(0, playerInfoData)
        }

        @EventHandler
        fun on(event: PlayerJoinEvent) {
            val profile = event.player.profile()

            event.joinMessage(
                text().color(YELLOW)
                    .append(
                        Component.translatable("multiplayer.player.joined").args(profile.nameComponent(showSuffix = false))
                    )
                    .build()
            )

            val textureProperty = event.player.playerProfile.properties.firstOrNull { it.name == "textures" } ?: return
            val signature = textureProperty.signature ?: return

            this@MaskManager.nautilus.scheduler.launch {
                profile.update(
                    setValue(ProfileSnapshot::lastKnownSkin, SkinBlob(textureProperty.value, signature))
                )
            }

            event.player.setSkin(skin(profile))
        }

        @EventHandler
        fun on(event: PlayerQuitEvent) {
            val profile = event.player.profile()

            event.quitMessage(
                text().color(YELLOW)
                    .append(
                        Component.translatable("multiplayer.player.left").args(profile.nameComponent(showSuffix = false))
                    )
                    .build()
            )
        }

        @EventHandler
        fun on(event: ProfileUpdateEvent) {
            val profile = event.profile
            val prev = event.previous ?: return

            if (skin(profile) != skin(prev)) event.player?.setSkin(profile.maskSkin)
        }
    }
}