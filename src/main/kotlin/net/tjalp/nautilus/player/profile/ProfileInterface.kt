package net.tjalp.nautilus.player.profile

import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.interfaces.NautilusInterface
import net.tjalp.nautilus.util.ItemGenerator.clickable
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.player
import org.bukkit.Material
import org.bukkit.Sound
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.view.InterfaceView
import org.incendo.interfaces.kotlin.paper.asElement
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.pane.ChestPane
import java.time.LocalDateTime

class ProfileInterface(
    private val profile: ProfileSnapshot,
    private val playSound: Boolean = true
) : NautilusInterface<ChestPane>() {

    override fun `interface`(): Interface<ChestPane, PlayerViewer> {
        return buildChestInterface {
            title = text()
                .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false, showHover = false, isClickable = false))
                .append(text("'s profile"))
                .build()
            rows = 3

            withTransform { view ->
                val lastOnline = if (profile.player()?.isOnline == true) {
                    text("Currently online")
                } else {
                    text("Last online ${Nautilus.TIME_FORMAT.format(profile.lastOnline)}")
                }
                val headDescription = arrayOf(
                    text("• First joined ${Nautilus.TIME_FORMAT.format(profile.firstJoin)}"),
                    text("• ").append(lastOnline)
                )

                view[4, 1] = clickable(
                    material = Material.PLAYER_HEAD,
                    name = profile.nameComponent(useMask = false, showSuffix = false, showHover = false, isClickable = false),
                    description = headDescription,
                    clickTo = text("send a Teleport Request")
                ).skull(profile.lastKnownSkin).build().asElement()
            }
        }
    }

    override fun open(viewer: PlayerViewer): InterfaceView<ChestPane, PlayerViewer> {
        val player = viewer.player()

        if (playSound) player.playSound(player.location, Sound.UI_LOOM_SELECT_PATTERN, 10f, 2f)

        return super.open(viewer)
    }
}