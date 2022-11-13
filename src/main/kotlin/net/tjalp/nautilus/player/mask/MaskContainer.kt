package net.tjalp.nautilus.player.mask

import kotlinx.coroutines.launch
import net.kyori.adventure.sound.Sound.Source.MASTER
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.container.Blueprint
import net.tjalp.nautilus.container.Container
import net.tjalp.nautilus.container.ContainerSlot
import net.tjalp.nautilus.container.PageableContainer
import net.tjalp.nautilus.permission.PermissionRank
import net.tjalp.nautilus.util.ItemGenerator.clickable
import net.tjalp.nautilus.util.TextInput
import net.tjalp.nautilus.util.profile
import org.bukkit.Material.*
import org.bukkit.Sound.UI_BUTTON_CLICK
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

class MaskContainer : Container(
    title = text("Mask"),
    rows = 4
) {

    private val nautilus = Nautilus.get()

    private var maskName: String? = null
    private var maskSkin: String? = null
    private var maskRank: PermissionRank? = null

    override fun render(player: Player, blueprint: Blueprint) {
        val name = ContainerSlot(
            clickable(
                material = OAK_SIGN,
                name = text("Username"),
                description = text("Change your display name"),
                clickTo = text("Change")
            ).build()
        ) { click ->
            val clicker = click.player

            TextInput.signSmall(clicker, label = text("Mask Name")) {
                val text = plainText().serialize(it)

                if (text.isNotBlank() && text.length <= 16) this.maskName = text
                else clicker.sendMessage(text("That username is not allowed!").color(RED))

                open(clicker)
            }
        }

        val skin = ContainerSlot(
            clickable(
                material = ARMOR_STAND,
                name = text("Skin"),
                description = text("Change your skin"),
                clickTo = text("Change")
            ).build()
        ) { click ->
            val clicker = click.player

            TextInput.signSmall(clicker, label = text("Mask Skin")) {
                val text = plainText().serialize(it)

                if (text.isNotBlank()) this.maskSkin = text

                open(clicker)
            }
        }

        val rank = ContainerSlot(
            clickable(
                material = DIAMOND,
                name = text("Rank"),
                description = text("Change your display rank"),
                clickTo = text("Change")
            ).build()
        ) { click ->
            val clicker = click.player

            RankContainer(clicker).open(clicker)
//            TextInput.signSmall(clicker, label = text("Mask Rank")) {
//                val text = plainText().serialize(it).lowercase()
//
//                if (text.isNotBlank() && this.nautilus.perms.rankExists(text)) {
//                    this.maskRank = this.nautilus.perms.getRank(text)
//                } else clicker.sendMessage(text("That rank does not exist!").color(RED))
//
//                open(clicker)
//            }
        }

        val apply = ContainerSlot(
            clickable(
                material = EMERALD,
                name = text("Apply"),
                description = text("Apply your mask modifications"),
                clickTo = text("Apply")
            ).build()
        ) {
            this.close()

            this.nautilus.scheduler.launch {
                nautilus.masking.mask(
                    profile = it.player.profile(),
                    username = maskName,
                    skin = maskSkin,
                    rank = maskRank,
                    message = true
                )
            }
        }

        blueprint
            .slot(11).set(name).clickSound()
            .slot(13).set(skin).clickSound()
            .slot(15).set(rank).clickSound()
            .slot(31).set(apply).clickSound()
    }

    private inner class RankContainer(val player: Player) : PageableContainer(
        title = text("Ranks"),
        rows = 3,
        fillableSlots = 11..15
    ) {

        init {
            val slots = this@MaskContainer.nautilus.perms.ranks
                .sortedByDescending { it.weight }
                .map { generateItem(it) }

            slots(slots, false)
        }

        private fun generateItem(rank: PermissionRank): ContainerSlot {
            return ContainerSlot(
                clickable(
                    material = LEATHER_CHESTPLATE,
                    name = rank.prefix,
                    description = text()
                        .append(text("Select the "))
                        .append(rank.prefix)
                        .append(text(" rank as your mask rank"))
                        .build(),
                    clickTo = text("Select")
                ).color(rank.nameColor).flags(*ItemFlag.values()).build(),
                sound = sound(UI_BUTTON_CLICK.key(), MASTER, 1f, 1f)
            ) {
                this@MaskContainer.maskRank = rank
                this@MaskContainer.open(this.player)
            }
        }
    }
}