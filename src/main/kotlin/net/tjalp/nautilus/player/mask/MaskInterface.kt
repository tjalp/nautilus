package net.tjalp.nautilus.player.mask

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.transform.types.PaginatedTransform
import org.incendo.interfaces.core.util.Vector2
import org.incendo.interfaces.kotlin.paper.asElement
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement
import org.incendo.interfaces.paper.pane.ChestPane

class MaskInterface : NautilusInterface<ChestPane>() {

    private val nautilus = Nautilus.get()

    private var maskName: String? = null
    private var maskSkin: String? = null
    private var maskRank: PermissionRank? = null

    override fun `interface`(): Interface<ChestPane, PlayerViewer> {
        return buildChestInterface {
            title = text("Mask")
            rows = 4

            withTransform { view ->
                view[2, 1] = clickable(
                    material = Material.OAK_SIGN,
                    name = text("Username"),
                    description = text("Change your display name"),
                    clickTo = text("Change")
                ).build().asElement { click ->
                    val viewer = click.viewer()
                    val clicker = viewer.player()
                    clicker.playClickSound()

                    TextInput.signSmall(clicker, label = text("Mask Name")) {
                        val text = plainText().serialize(it)

                        if (text.isNotBlank()) {
                            if (text.length <= 16) maskName = text
                            else clicker.sendMessage(text("That username is not allowed!").color(NamedTextColor.RED))
                        }

                        open(viewer)
                    }
                }

                view[4, 1] = clickable(
                    material = Material.ARMOR_STAND,
                    name = text("Skin"),
                    description = text("Change your skin"),
                    clickTo = text("Change")
                ).build().asElement { click ->
                    val viewer = click.viewer()
                    val clicker = viewer.player()
                    clicker.playClickSound()

                    TextInput.signSmall(clicker, label = text("Mask Skin")) {
                        val text = plainText().serialize(it)

                        if (text.isNotBlank()) maskSkin = text

                        open(viewer)
                    }
                }

                view[6, 1] = clickable(
                    material = Material.DIAMOND,
                    name = text("Rank"),
                    description = text("Change your display rank"),
                    clickTo = text("Change")
                ).build().asElement { click ->
                    click.viewer().player().playClickSound()

                    RankInterface().open(click.viewer())
                }

                val applyDescription = mutableListOf(text("Apply your mask modifications"))
                if (maskName != null) applyDescription += text("• Name ($maskName)")
                if (maskSkin != null) applyDescription += text("• Skin: ($maskSkin)")
                if (maskRank != null) applyDescription += text("• Name: (").append(maskRank!!.prefix).append(text(")"))

                view[4, 3] = clickable(
                    material = Material.EMERALD,
                    name = text("Apply"),
                    description = applyDescription.toTypedArray(),
                    clickTo = text("Apply")
                ).build().asElement { click ->
                    click.viewer().close()
                    click.viewer().player().playClickSound()

                    nautilus.scheduler.launch {
                        nautilus.masking.mask(
                            profile = click.viewer().player().profile(),
                            username = maskName,
                            skin = maskSkin,
                            rank = maskRank,
                            message = true
                        )
                    }
                }
            }
        }
    }

    private inner class RankInterface : NautilusInterface<ChestPane>() {

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

                return@map clickable.asElement<ChestPane> { click ->
                    click.viewer().player().playClickSound()
                    maskRank = rank
                    this@MaskInterface.open(click.viewer())
                }
            }

        override fun `interface`(): Interface<ChestPane, PlayerViewer> {
            return buildChestInterface {
                title = text("Rank Mask")
                rows = 3

                val pageTransform = PaginatedTransform<ItemStackElement<ChestPane>, ChestPane, PlayerViewer>(
                    Vector2.at(2, 1),
                    Vector2.at(6, 1),
                    this@RankInterface.ranks
                )

                pageTransform.backwardElement(Vector2.at(3, 2)) { transform ->
                    clickable(
                        material = Material.ARROW,
                        name = text("Previous Page"),
                        description = text("Return to the previous page"),
                        clickTo = text("Move")
                    ).build().asElement {
                        it.viewer().player().playClickSound()
                        transform.previousPage()
                    }
                }

                pageTransform.forwardElement(Vector2.at(5, 2)) { transform ->
                    clickable(
                        material = Material.ARROW,
                        name = text("Next Page"),
                        description = text("Continue on the next page"),
                        clickTo = text("Move")
                    ).build().asElement {
                        it.viewer().player().playClickSound()
                        transform.nextPage()
                    }
                }

                addTransform(pageTransform)

                withTransform { view ->
                    view[0, 2] = clickable(
                        material = Material.ARROW,
                        name = text("Return"),
                        description = text("Return to the previous menu"),
                        clickTo = text("Return")
                    ).build().asElement {
                        it.viewer().player().playClickSound()
                        this@MaskInterface.open(it.viewer())
                    }
                }
            }
        }
    }
}