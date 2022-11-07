package net.tjalp.nautilus.player.profile

import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.container.Blueprint
import net.tjalp.nautilus.container.Container
import net.tjalp.nautilus.container.ContainerSlot
import net.tjalp.nautilus.util.ItemGenerator
import net.tjalp.nautilus.util.nameComponent
import org.bukkit.Material.PLAYER_HEAD
import org.bukkit.entity.Player

class ProfileContainer(
    val profile: ProfileSnapshot
) : Container(
    title = text()
        .append(profile.nameComponent(useMask = false))
        .append(text("'s profile"))
        .build(),
    rows = 3
) {

    override fun render(player: Player, blueprint: Blueprint) {
        val head = ContainerSlot(
            ItemGenerator.clickable(
                material = PLAYER_HEAD,
                name = profile.nameComponent(useMask = false, showSuffix = false),
                description = text("Contains data: ${profile.data.toString()}"),
                clickTo = text("View")
            ).skull(profile.lastKnownSkin).build()
        ) {
            val clicker = it.player

            clicker.closeInventory()
            clicker.sendMessage(text("The text value is: ${profile.data.toString()}"))
        }

        blueprint.slot(13).set(head)
    }
}