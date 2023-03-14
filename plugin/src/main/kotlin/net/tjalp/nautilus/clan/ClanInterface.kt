package net.tjalp.nautilus.clan

import net.kyori.adventure.text.Component.text
import net.tjalp.nautilus.interfaces.NautilusInterface
import net.tjalp.nautilus.util.ItemGenerator.clickable
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.interfaces.next.drawable.Drawable.Companion.drawable
import org.incendo.interfaces.next.element.StaticElement
import org.incendo.interfaces.next.interfaces.Interface
import org.incendo.interfaces.next.interfaces.buildChestInterface

class ClanInterface(
    private val clan: ClanSnapshot
) : NautilusInterface {

    private val icon: ItemStack; get() = clickable(
        material = Material.TURTLE_EGG,
        name = text(clan.name, clan.theme()),
        clickTo = text("do nothing")
    ).build()

    override fun create(): Interface<*> = buildChestInterface {
        initialTitle = text(clan.name, clan.theme())
        rows = 3

        withTransform { pane, view ->
            pane[1, 4] = StaticElement(drawable(icon))
            if (view.parent() != null) pane[2, 0] = backElement()
        }
    }

    companion object {
        fun playOpenSound(player: Player) {
            player.playSound(player.location, Sound.UI_LOOM_SELECT_PATTERN, 10f, 2f)
        }
    }
}