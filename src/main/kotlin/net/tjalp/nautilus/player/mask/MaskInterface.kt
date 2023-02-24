package net.tjalp.nautilus.player.mask

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.interfaces.NautilusInterface
import net.tjalp.nautilus.permission.PermissionRank
import net.tjalp.nautilus.util.ItemGenerator.clickable
import net.tjalp.nautilus.util.TextInput
import net.tjalp.nautilus.util.playClickSound
import net.tjalp.nautilus.util.profile
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.incendo.interfaces.next.drawable.Drawable.Companion.drawable
import org.incendo.interfaces.next.element.StaticElement
import org.incendo.interfaces.next.grid.GridBoxGenerator
import org.incendo.interfaces.next.grid.GridPoint
import org.incendo.interfaces.next.interfaces.Interface
import org.incendo.interfaces.next.interfaces.buildChestInterface
import org.incendo.interfaces.next.pane.ChestPane
import org.incendo.interfaces.next.transform.builtin.PaginationButton
import org.incendo.interfaces.next.transform.builtin.PaginationTransformation

class MaskInterface(
    private val nautilus: Nautilus
) : NautilusInterface {

    private var maskName: String? = null
    private var maskSkin: String? = null
    private var maskRank: PermissionRank? = null

    override fun create(): Interface<*> = buildChestInterface {
        initialTitle = text("Mask")
        rows = 4

        withTransform { pane, view ->
            pane[1, 2] = usernameElement()
            pane[1, 4] = skinElement()
            pane[1, 6] = rankElement()
            pane[3, 4] = applyElement()
            if (view.parent() != null) pane[3, 0] = backElement()
        }
    }

    private fun usernameElement(): StaticElement {
        return StaticElement(drawable(clickable(
            material = Material.OAK_SIGN,
            name = text("Username"),
            description = text("Change your display name"),
            clickTo = text("change")
        ).build())) { click ->
            val player = click.player

            player.playClickSound()

            TextInput.signSmall(player = player, label = text("Username")) {
                val text = plainText().serialize(it)

                if (text.isNotBlank()) {
                    if (text.length <= 16) maskName = text
                    else player.sendMessage(text("That username is not allowed!").color(RED))
                }

                click.view.open()
            }
        }
    }

    private fun skinElement(): StaticElement {
        return StaticElement(drawable(clickable(
            material = Material.ARMOR_STAND,
            name = text("Skin"),
            description = text("Change your skin"),
            clickTo = text("change")
        ).build())) { click ->
            val player = click.player

            player.playClickSound()

            TextInput.signSmall(player = player, label = text("Mask Skin")) {
                val text = plainText().serialize(it)

                if (text.isNotBlank()) maskSkin = text

                click.view.open()
            }
        }
    }

    private fun rankElement(): StaticElement {
        return StaticElement(drawable(clickable(
            material = Material.DIAMOND,
            name = text("Rank"),
            description = text("Change your display rank"),
            clickTo = text("change")
        ).build())) { click ->
            click.player.playClickSound()

            this@MaskInterface.nautilus.scheduler.launch {
                RankInterface().create().open(click.player, click.view)
            }
        }
    }

    private fun applyElement(): StaticElement {
        val applyDescription = mutableListOf(text("Apply your mask modifications"))
        if (maskName != null) applyDescription += text("• Name ($maskName)")
        if (maskSkin != null) applyDescription += text("• Skin ($maskSkin)")
        if (maskRank != null) applyDescription += text("• Rank (").append(maskRank!!.prefix).append(text(")"))

        return StaticElement(drawable(clickable(
            material = Material.EMERALD,
            name = text("Apply"),
            description = applyDescription.toTypedArray(),
            clickTo = text("apply")
        ).build())) { click ->
            click.view.close()
            click.player.playClickSound()

            nautilus.scheduler.launch {
                nautilus.masking.mask(
                    profile = click.player.profile(),
                    username = maskName,
                    skin = maskSkin,
                    rank = maskRank,
                    message = true
                )
            }
        }
    }

    private inner class RankInterface : NautilusInterface {

        val ranks = nautilus.perms.ranks
            .sortedByDescending { it.weight }
            .map { rank ->
                val clickable = clickable(
                    material = Material.LEATHER_CHESTPLATE,
                    name = rank.prefix,
                    description = text()
                        .append(text("Select the "))
                        .append(rank.prefix)
                        .append(text(" rank as your mask rank"))
                        .build(),
                    clickTo = text("Select")
                ).color(rank.nameColor).flags(*ItemFlag.values()).build()

                return@map StaticElement(drawable(clickable)) { click ->
                    click.player.playClickSound()
                    maskRank = rank
                    click.view.back()
                }
            }

        override fun create(): Interface<*> = buildChestInterface {
            initialTitle = text("Mask Rank")
            rows = 3

            val paginationTransform = PaginationTransformation<ChestPane>(
                GridBoxGenerator(
                    GridPoint(2, 1),
                    GridPoint(6, 1)
                ),
                ranks,
                back = PaginationButton(GridPoint(3, 2), drawable(Material.ARROW), emptyMap()),
                forward = PaginationButton(GridPoint(5, 2), drawable(Material.ARROW), emptyMap()),
            )

            withTransform(transform = paginationTransform)
            withTransform { pane, view ->
                if (view.parent() != null) pane[2, 0] = backElement()
            }
        }
    }
}