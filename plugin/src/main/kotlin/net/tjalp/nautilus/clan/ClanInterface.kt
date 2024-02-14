package net.tjalp.nautilus.clan

import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.interfaces.NautilusInterface
import net.tjalp.nautilus.util.ItemGenerator.clickable
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement
import org.incendo.interfaces.paper.pane.ChestPane

class ClanInterface(
    private val parent: Interface<*, PlayerViewer>?,
    private val clan: ClanSnapshot
) : NautilusInterface<ChestPane> {

    private val icon: ItemStack; get() = clickable(
        material = Material.TURTLE_EGG,
        name = text(clan.name, clan.theme()),
        clickTo = text("do nothing")
    ).build()

    override fun create() = buildChestInterface {
        title = text(clan.name, clan.theme())
        rows = 3

        withTransform { view ->
            view[4, 1] = ItemStackElement(icon)
            if (parent() != null) view[0, 2] = backElement()
        }
    }

    override fun parent() = this.parent

    companion object {
        fun playOpenSound(player: Player) {
            player.playSound(player.location, Sound.UI_LOOM_SELECT_PATTERN, 10f, 2f)
        }
    }
}