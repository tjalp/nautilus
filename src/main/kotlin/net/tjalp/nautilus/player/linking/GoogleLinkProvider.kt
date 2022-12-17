package net.tjalp.nautilus.player.linking

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.ClickEvent.openUrl
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.UNDERLINED
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.database.MongoCollections
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.profile
import net.tjalp.nautilus.util.register
import org.bson.types.ObjectId
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.litote.kmongo.reactivestreams.save
import org.litote.kmongo.setValue
import java.util.*

class GoogleLinkProvider(private val nautilus: Nautilus) : LinkProvider<ObjectId> {

    init {
        GoogleLinkProviderListener().register()
    }

    override fun isLinked(profile: ProfileSnapshot): Boolean {
        return profile.googleUser != null
    }

    override suspend fun link(profile: ProfileSnapshot, link: ObjectId) {
        profile.update(setValue(ProfileSnapshot::googleUser, link))
    }

    override fun link(profile: ProfileSnapshot): ObjectId? {
        return profile.googleUser
    }

    private inner class GoogleLinkProviderListener : Listener {

        // Necessary because for some reason the PlayerLoginEvent gets called twice when disallowed
        private val uniqueIdCache = mutableMapOf<UUID, String>()

        @EventHandler
        fun on(event: PlayerLoginEvent) {
            val player = event.player
            val uniqueId = player.uniqueId
            val profile = player.profile()
            val isBedrock = nautilus.floodgate?.isFloodgateId(uniqueId) == true

            if (event.result != PlayerLoginEvent.Result.ALLOWED || isLinked(profile)) return

            val randomToken = uniqueIdCache[uniqueId] ?: UUID.randomUUID().toString().take(6)

            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, generateNotLinkedMessage(randomToken, isBedrock))

            this@GoogleLinkProvider.nautilus.scheduler.launch {
                if (uniqueId in uniqueIdCache) return@launch

                uniqueIdCache[uniqueId] = randomToken

                val googleLinkToken = GoogleLinkToken(uniqueId, randomToken)

                launch {
                    delay(30_000)
                    uniqueIdCache -= uniqueId
                }

                MongoCollections.linkTokens.save(googleLinkToken).awaitSingle()
            }
        }

        private fun generateNotLinkedMessage(token: String, isBedrock: Boolean = false): Component {
            val builder = text().color(GRAY)

            if (isBedrock) {
                builder.append(text("You do not have a Google Account linked to your Minecraft account!", RED, BOLD))
                    .append(newline()).append(text("Navigate to ")
                        .append(text("https://example.com/", AQUA, UNDERLINED)
                            .clickEvent(openUrl("https://example.com/")))
                        .append(text(" and enter the following code:")))
                    .append(newline()).append(text("→ ", WHITE).append(text(token, GOLD, BOLD)).append(text(" ←")))
                return builder.build()
            }

            builder.append(text("You do not have a Google Account linked to your Minecraft account!", RED, BOLD))
                .append(newline()).append(text("We need to know your identity so we can know that you are"))
                .append(newline()).append(text("actually a student from a verified school and not an imposter."))
                .append(newline())
                .append(newline()).append(text("To link an account, please navigate to"))
                .append(newline()).append(text("https://example.com/", AQUA, UNDERLINED)
                    .clickEvent(openUrl("https://example.com/")))
                .append(newline()).append(text("and enter the following token:"))
                .append(newline())
                .append(newline()).append(text("→ ", WHITE).append(text(token, GOLD, BOLD)).append(text(" ←")))
                .append(newline())
                .append(newline()).append(text("THANK YOU!", GREEN, BOLD))

            return builder.build()
        }
    }
}