package net.tjalp.nautilus.ktor

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.config.details.ResourcePackDetails
import net.tjalp.nautilus.util.isUniqueId
import net.tjalp.nautilus.util.register
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.math.BigInteger
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
    private val nautilus: Nautilus,
    private val details: ResourcePackDetails
) {

    private var server: NettyApplicationEngine = embeddedServer(Netty, environment = applicationEngineEnvironment {
        log = nautilus.slF4JLogger

        module {
            routing {
                get(details.hostPath) {
                    call.respondFile(packPath.toFile())
                }
                get("/profile/{target}/update") {
                    updateProfile(call.parameters["target"] ?: return@get)
                }
            }
        }

        connector {
            port = details.hostPort
            host = details.host
        }
    })

    private val packPath: Path
    private val hash: String

    init {
        val pack = this.nautilus.dataFolder.toPath().resolve(details.fileName)

        if (pack.notExists()) throw IllegalStateException("No ${details.fileName} found!")

        this.packPath = pack

        // I have absolutely no idea what's going on here, but it works??
        val digest = MessageDigest.getInstance("SHA-1").digest(pack.readBytes())
        this.hash = BigInteger(1, digest).toString(16).padStart(32, '0')

        ResourcePackListener().register()
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

    private fun updateProfile(target: String) {
        if (target.isUniqueId()) {
            val uniqueId = UUID.fromString(target)
            this.nautilus.scheduler.launch { nautilus.profiles.profileIfCached(uniqueId)?.update() }
            return
        }

        this.nautilus.scheduler.launch { nautilus.profiles.profileIfCached(target)?.update() }
    }

    // todo move this somewhere else
    private inner class ResourcePackListener : Listener {

        @EventHandler
        fun on(event: PlayerJoinEvent) {
            val player = event.player
            val hostname = player.virtualHost?.hostName ?: return

            nautilus.logger.info("Hostname is $hostname")

            if (details.overrideUrl.isNotBlank()) {
                player.setResourcePack(details.overrideUrl, hash, true)
                return
            }
            player.setResourcePack("http://$hostname:${details.hostPort}${details.hostPath}", hash, true)
        }
    }
}