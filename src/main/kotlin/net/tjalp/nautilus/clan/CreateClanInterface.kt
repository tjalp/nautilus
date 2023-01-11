package net.tjalp.nautilus.clan

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.interfaces.NautilusInterface
import net.tjalp.nautilus.player.profile.ProfileSnapshot
import net.tjalp.nautilus.util.ItemGenerator.clickable
import net.tjalp.nautilus.util.TextInput
import net.tjalp.nautilus.util.playClickSound
import net.tjalp.nautilus.util.profile
import org.bukkit.Material
import org.bukkit.entity.Player
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.kotlin.paper.asElement
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.pane.ChestPane
import org.litote.kmongo.setValue

class CreateClanInterface(
    private val nautilus: Nautilus
) : NautilusInterface<ChestPane>() {

    private val clans = this.nautilus.clans
    private val scheduler = this.nautilus.scheduler
    private var name: String? = null
    private var theme: String? = null

    override fun `interface`(): Interface<ChestPane, PlayerViewer> {
        return buildChestInterface {
            title = text("Create Clan")
            rows = 4

            withTransform { view ->
                view[3, 1] = clickable(
                    material = Material.OAK_SIGN,
                    name = text("Clan Name"),
                    clickTo = text("set a name")
                ).build().asElement { click ->
                    val viewer = click.viewer()
                    val player = viewer.player()
                    player.playClickSound()

                    TextInput.signSmall(player = player, label = text("Clan Name")) {
                        val text = plainText().serialize(it)

                        if (text.isBlank() || text.length < 5 || text.length > 20) {
                            player.sendMessage(text("A clan name must be between 5 and 20 characters in length", RED))
                            open(viewer)
                            return@signSmall
                        }

                        this@CreateClanInterface.name = text
                        open(viewer)
                    }
                }

                view[5, 1] = clickable(
                    material = Material.LIME_WOOL,
                    name = text("Theme"),
                    description = text("Personalize your clan with a color"),
                    clickTo = text("set a color")
                ).build().asElement { click ->
                    val viewer = click.viewer()
                    val player = viewer.player()
                    player.playClickSound()

                    TextInput.signSmall(player = player, label = text("Hex Color")) {
                        val text = plainText().serialize(it)
                        val isValid = TextColor.fromHexString(text) != null

                        if (text.isBlank() || !isValid) {
                            player.sendMessage(text("That is not a valid hex color string!", RED))
                            open(viewer)
                            return@signSmall
                        }

                        this@CreateClanInterface.theme = text
                        open(viewer)
                    }
                }

                view[4, 3] = clickable(
                    material = Material.LIME_CANDLE,
                    name = text("Create your clan"),
                    clickTo = text("create your clan")
                ).build().asElement { click ->
                    val player = click.viewer().player()

                    if (!createClan(player, name)) return@asElement

                    click.viewer().close()
                    player.playClickSound()
                }
            }
        }
    }

    private fun createClan(player: Player, name: String?): Boolean {
        if (name == null) {
            player.sendMessage(text("You didn't enter a valid name for your clan!", RED))
            return false
        }

        scheduler.launch {
            player.sendMessage(text("Creating your clan...", GRAY))

            val clan = clans.createClan(leader = player.uniqueId, name = name, theme = theme)
            player.profile().update(setValue(property = ProfileSnapshot::clanId, value = clan.id))

            player.sendMessage(text("Created your clan with the name '$name'", GREEN))
        }

        return true
    }
}