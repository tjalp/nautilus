package net.tjalp.nautilus.clan

import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.interfaces.NautilusInterface
import net.tjalp.nautilus.util.ItemGenerator.clickable
import org.bukkit.Material
import org.bukkit.Sound
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.view.InterfaceView
import org.incendo.interfaces.kotlin.paper.asElement
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.pane.ChestPane

class ClanInterface(
    private val clan: ClanSnapshot,
    private val playSound: Boolean = true
) : NautilusInterface<ChestPane>() {

    override fun `interface`(): Interface<ChestPane, PlayerViewer> {
        return buildChestInterface {
            title = text(clan.name).color(clan.theme())
            rows = 3

            withTransform { view ->
                view[4, 1] = clickable(
                    material = Material.TURTLE_EGG,
                    name = text(clan.name).color(clan.theme()),
                    clickTo = text("do nothing")
                ).build().asElement()
            }
        }
    }

    override fun open(viewer: PlayerViewer): InterfaceView<ChestPane, PlayerViewer> {
        val player = viewer.player()

        if (playSound) player.playSound(player.location, Sound.UI_LOOM_SELECT_PATTERN, 10f, 2f)

        return super.open(viewer)
    }
}