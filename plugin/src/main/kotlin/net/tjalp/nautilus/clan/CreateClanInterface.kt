package net.tjalp.nautilus.clan

import com.destroystokyo.paper.MaterialTags
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement
import org.incendo.interfaces.paper.pane.ChestPane
import org.litote.kmongo.setValue

class CreateClanInterface(
    private val parent: Interface<*, PlayerViewer>?,
    private val nautilus: Nautilus
) : NautilusInterface<ChestPane> {

    private val clans = this.nautilus.clans
    private val scheduler = this.nautilus.scheduler
    private var name: String? = null
    private var theme: String? = null

    override fun create() = buildChestInterface {
        title = text("Create Clan")
        rows = 4

        withTransform { view ->
            view[3, 1] = nameElement()
            view[5, 1] = themeElement()
            view[4, 3] = createElement()
            if (parent() != null) view[0, 3] = backElement()
        }
    }

    override fun parent() = this.parent

    private fun nameElement(): ItemStackElement<ChestPane> {
        return ItemStackElement(clickable(
            material = Material.NAME_TAG,
            name = text("Clan Name"),
            description = arrayOf(text("Enter a name for your clan")),
            clickTo = text("set a name")).build()
        ) { click ->
            val player = click.viewer().player()
            player.playClickSound()

            TextInput.signSmall(player = player, label = text("Clan Name")) {
                val text = plainText().serialize(it)

                if (text.isBlank() || text.length < 5 || text.length > 20) {
                    player.sendMessage(text("A clan name must be between 5 and 20 characters in length", NamedTextColor.RED))
                    click.view().open()
                    return@signSmall
                }

                name = text
                click.view().open()
            }
        }
    }

    private fun themeElement(): ItemStackElement<ChestPane> {
        return ItemStackElement(clickable(
            material = MaterialTags.DYES.values.random(),
            name = text("Theme"),
            description = text("Personalize your clan with a color"),
            clickTo = text("set a color")
        ).build()) { click ->
            val player = click.viewer().player()
            player.playClickSound()

            TextInput.signSmall(player = player, label = text("Hex Color")) {
                val text = plainText().serialize(it)
                val isValid = TextColor.fromHexString(text) != null

                if (text.isBlank() || !isValid) {
                    player.sendMessage(text("That is not a valid hex color string!", NamedTextColor.RED))
                    click.view().open()
                    return@signSmall
                }

                theme = text
                click.view().open()
            }
        }
    }

    private fun createElement(): ItemStackElement<ChestPane> {
        return ItemStackElement(clickable(
            material = Material.EMERALD,
            name = text("Create"),
            description = text("Create your clan"),
            clickTo = text("create your clan")
        ).build()) { click ->
            val player = click.viewer().player()

            if (!createClan(player)) return@ItemStackElement

            click.viewer().close()
            player.playClickSound()
        }
    }

    private fun createClan(player: Player): Boolean {
        val localName = name
        if (localName == null) {
            player.sendMessage(text("You didn't enter a valid name for your clan!", NamedTextColor.RED))
            return false
        }

        scheduler.launch {
            player.sendMessage(text("Creating your clan...", NamedTextColor.GRAY))

            val clan = clans.createClan(leader = player.uniqueId, name = localName, theme = theme)
            player.profile().update(setValue(property = ProfileSnapshot::clanId, value = clan.id))

            player.sendMessage(text("Created your clan with the name '$localName'", NamedTextColor.GREEN))
        }

        return true
    }
}