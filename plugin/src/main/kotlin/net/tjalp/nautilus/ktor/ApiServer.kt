package net.tjalp.nautilus.ktor

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.config.details.ResourcePackDetails
import net.tjalp.nautilus.util.isUniqueId
import net.tjalp.nautilus.util.register
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.DECLINED
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.notExists
import kotlin.io.path.readBytes

/**
 * A server that serves the resource pack tied
 * to this particular version of Nautilus.
 */
class ApiServer(
    private val nautilus: Nautilus
) {

    private val packDetails: ResourcePackDetails = this.nautilus.config.resourcepack
    private var packPath: Path? = null
    private var packHash: ByteArray? = null

    private var server: NettyApplicationEngine = embeddedServer(Netty, environment = applicationEngineEnvironment {
        log = nautilus.slF4JLogger

        module {
            routing {
                if (packDetails.enabled) {
                    get(packDetails.hostPath) {
                        call.respondFile(packPath?.toFile() ?: return@get)
                    }
                }
                get("/profile/{target}/update") { // todo make this a post request
                    updateProfile(call.parameters["target"] ?: return@get)
                }
            }
        }

        connector {
            port = packDetails.hostPort
            host = packDetails.host
        }
    })

    init {
        if (packDetails.enabled) {
            val pack = this.nautilus.dataFolder.toPath().resolve(packDetails.fileName)

            if (pack.notExists()) throw IllegalStateException("No ${packDetails.fileName} found!")

            this.packPath = pack

            // I have absolutely no idea what's going on here, but it works??
            // UPDATE: I've figured out what's going on
            // SECOND UPDATE: I've now figured out how to make it work without the weirdness
            val byteArray = if (packDetails.hash.isNotEmpty()) packDetails.hash.toByteArray() else null
            this.packHash = byteArray ?: MessageDigest.getInstance("SHA-1").digest(pack.readBytes())

            ResourcePackListener().register()
        }
    }

    /**
     * Start the API server. This will start the Ktor server
     * on another thread, so it's safe to call this from anywhere.
     *
     * Please note that the startup sequence is however on the
     * same thread as the one that called this method.
     */
    fun start() {
        this.nautilus.logger.info("--- Starting API server... ---")
        this.server.start(wait = false)
    }

    /**
     * Stop the API server.
     */
    fun stop() {
        this.nautilus.logger.info("Stopping API server...")
        this.server.stop()
    }

    private suspend fun updateProfile(target: String) {
        this.nautilus.logger.info("Received profile update request (target: $target)")

        val profile =
            if (target.isUniqueId()) {
                val uniqueId = UUID.fromString(target)
                this.nautilus.profiles.profileIfCached(uniqueId)
            } else this.nautilus.profiles.profileIfCached(target)

        profile?.update() ?: this.nautilus.logger.info("No cached profile found for '$target'")
    }

    // todo move this somewhere else
    private inner class ResourcePackListener : Listener {

        @EventHandler
        fun on(event: PlayerJoinEvent) {
            val player = event.player
            val hostname = player.virtualHost?.hostName ?: return

            if (packDetails.overrideUrl.isNotBlank()) {
                player.setResourcePack(packDetails.overrideUrl, packHash, empty(), true)
                return
            }
            player.setResourcePack("http://$hostname:${packDetails.hostPort}${packDetails.hostPath}", packHash, empty(), true)
        }

        @EventHandler
        fun on(event: PlayerResourcePackStatusEvent) {
            val player = event.player

            when (event.status) {
                DECLINED -> player.kick(text("You must accept the resource pack in order to play!", RED))
                FAILED_DOWNLOAD -> player.sendMessage(
                    text(
                        "Your resource pack download failed, " +
                                "so any functionality that uses custom resources might not work as expected. " +
                                "You'll still be able to play on this server, but with limited functionality. " +
                                "This may be different in the future as we're moving out of the alpha/beta phase.", RED
                    )
                )

                else -> {}
            }
        }
    }
}