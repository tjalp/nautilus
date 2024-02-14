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
import org.bukkit.inventory.ItemStack
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.transform.types.PaginatedTransform
import org.incendo.interfaces.core.util.Vector2
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement
import org.incendo.interfaces.paper.pane.ChestPane

class MaskInterface(
    private val parent: Interface<*, PlayerViewer>?,
    private val nautilus: Nautilus
) : NautilusInterface<ChestPane> {

    private var maskName: String? = null
    private var maskSkin: String? = null
    private var maskRank: PermissionRank? = null

    override fun create() = buildChestInterface {
        title = text("Mask")
        rows = 4

        withTransform { view ->
            view[2, 1] = usernameElement()
            view[4, 1] = skinElement()
            view[6, 1] = rankElement()
            view[4, 3] = applyElement()
            if (parent() != null) view[0, 3] = backElement()
        }
    }

    override fun parent() = this.parent

    private fun usernameElement(): ItemStackElement<ChestPane> {
        return ItemStackElement(clickable(
            material = Material.OAK_SIGN,
            name = text("Username"),
            description = text("Change your display name"),
            clickTo = text("change")
        ).build()) { click ->
            val player = click.viewer().player()

            player.playClickSound()

            TextInput.signSmall(player = player, label = text("Username")) {
                val text = plainText().serialize(it)

                if (text.isNotBlank()) {
                    if (text.length <= 16) maskName = text
                    else player.sendMessage(text("That username is not allowed!").color(RED))
                }

                click.view().open()
            }
        }
    }

    private fun skinElement(): ItemStackElement<ChestPane> {
        return ItemStackElement<ChestPane>(clickable(
            material = Material.ARMOR_STAND,
            name = text("Skin"),
            description = text("Change your skin"),
            clickTo = text("change")
        ).build()) { click ->
            val player = click.viewer().player()

            player.playClickSound()

            TextInput.signSmall(player = player, label = text("Mask Skin")) {
                val text = plainText().serialize(it)

                if (text.isNotBlank()) maskSkin = text

                click.view().open()
            }
        }
    }

    private fun rankElement(): ItemStackElement<ChestPane> {
        return ItemStackElement(clickable(
            material = Material.DIAMOND,
            name = text("Rank"),
            description = text("Change your display rank"),
            clickTo = text("change")
        ).build()) { click ->
            click.viewer().player().playClickSound()

            this@MaskInterface.nautilus.scheduler.launch {
                RankInterface(click.view().backing()).create().open(click.viewer())
            }
        }
    }

    private fun applyElement(): ItemStackElement<ChestPane> {
        val applyDescription = mutableListOf(text("Apply your mask modifications"))
        if (maskName != null) applyDescription += text("• Name ($maskName)")
        if (maskSkin != null) applyDescription += text("• Skin ($maskSkin)")
        if (maskRank != null) applyDescription += text("• Rank (").append(maskRank!!.prefix).append(text(")"))

        return ItemStackElement(clickable(
            material = Material.EMERALD,
            name = text("Apply"),
            description = applyDescription.toTypedArray(),
            clickTo = text("apply")
        ).build()) { click ->
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

    private inner class RankInterface(private val parent: Interface<*, PlayerViewer>?) : NautilusInterface<ChestPane> {

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
                ).color(rank.nameColor).flags(*ItemFlag.entries.toTypedArray()).build()

                return@map ItemStackElement<ChestPane>(clickable) { click ->
                    click.viewer().player().playClickSound()
                    maskRank = rank
                    this.parent()?.open(click.viewer())
                }
            }

        override fun create() = buildChestInterface {
            title = text("Mask Rank")
            rows = 3

            val paginationTransform = PaginatedTransform<ItemStackElement<ChestPane>, ChestPane, PlayerViewer>(
                Vector2.at(2, 1),
                Vector2.at(6, 1),
                ranks
            ).apply {
                backwardElement(Vector2.at(3, 2)) { transform ->
                    ItemStackElement(ItemStack(Material.ARROW)) {
                        transform.previousPage()
                    }
                }
                forwardElement(Vector2.at(5, 2)) { transform ->
                    ItemStackElement(ItemStack(Material.ARROW)) {
                        transform.nextPage()
                    }
                }
            }

            addTransform(paginationTransform)

            withTransform { view ->
                if (parent() != null) view[0, 2] = backElement()
            }
        }

        override fun parent() = this@RankInterface.parent
    }
}