package net.tjalp.nautilus.player.profile

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor.color
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.clan.ClanInterface
import net.tjalp.nautilus.interfaces.NautilusInterface
import net.tjalp.nautilus.player.teleport.PlayerTeleportRequest
import net.tjalp.nautilus.util.ItemBuilder
import net.tjalp.nautilus.util.ItemGenerator.clickable
import net.tjalp.nautilus.util.nameComponent
import net.tjalp.nautilus.util.playClickSound
import net.tjalp.nautilus.util.player
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.interfaces.next.drawable.Drawable.Companion.drawable
import org.incendo.interfaces.next.element.StaticElement
import org.incendo.interfaces.next.interfaces.Interface
import org.incendo.interfaces.next.interfaces.buildChestInterface

class ProfileInterface(
    private val nautilus: Nautilus,
    val profile: ProfileSnapshot
) : NautilusInterface {

    private val lastOnline: Component; get() {
        return if (profile.player()?.isOnline == true) {
            text("Currently online")
        } else {
            text("Last online ${Nautilus.TIME_FORMAT.format(profile.lastOnline)}")
        }
    }
    private val skull: ItemStack; get() {
        return ItemBuilder(Material.PLAYER_HEAD)
            .skull(profile.lastKnownSkin)
            .name(profile.nameComponent(useMask = false, showSuffix = false, showHover = false, isClickable = false))
            .lore(
                empty(),
                text("• First joined ${Nautilus.TIME_FORMAT.format(profile.firstJoin)}", color(251, 228, 96)),
                text("• ", color(251, 228, 96)).append(lastOnline)
            )
            .build()
    }
    private val loadingClanElement: StaticElement; get() {
        return StaticElement(drawable(clickable(
            material = Material.TOTEM_OF_UNDYING,
            name = text("Clan"),
            description = text("Loading..."),
            clickTo = text("do nothing")
        ).build()))
    }

    override fun create(): Interface<*> = buildChestInterface {
        initialTitle = text()
            .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false, showHover = false, isClickable = false))
            .append(text("'s profile"))
            .build()
        rows = 4

        withTransform { pane, _ ->
            pane[2, 5] = clanElement()
        }

        withTransform { pane, view ->
            pane[0, 4] = StaticElement(drawable(skull))
            pane[2, 3] = teleportElement()
            pane[2, 5] = loadingClanElement
            if (view.parent() != null) pane[3, 0] = backElement()
        }

    }

    private fun teleportElement(): StaticElement {
        return StaticElement(drawable(clickable(
            material = Material.ENDER_PEARL,
            name = text("Teleport Request"),
            description = arrayOf(
                text("Send a request to teleport to this player"),
                empty(),
                text("Teleport requests must be accepted by"),
                text("the target player in order to teleport")
            ),
            clickTo = text("send a Teleport Request")
        ).build())) { click ->
            val player = click.player
            val target = this@ProfileInterface.profile.player()

            click.view.close()
            player.playClickSound()

            if (target == null || profile.maskName != null) {
                player.sendMessage(
                    text().color(RED)
                        .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                        .appendSpace().append(text("is not online at the moment"))
                )
                return@StaticElement
            }

            PlayerTeleportRequest(player, target).request()
        }
    }

    private suspend fun clanElement(): StaticElement {
        val clan = profile.clanId?.let { this.nautilus.clans.clan(it) } ?: return StaticElement(drawable(clickable(
            material = Material.TOTEM_OF_UNDYING,
            name = text("Clan"),
            description = text("This player is not in a clan"),
            clickTo = text("do nothing")
        ).build()))

        return StaticElement(drawable(clickable(
            material = Material.TOTEM_OF_UNDYING,
            name = text("Clan"),
            description = arrayOf(
                text("• Name: ").append(text(clan.name, clan.theme())),
                text("• Leaders: ").append(text(clan.leaders.size, clan.theme())),
                text("• Members: ").append(text(clan.members.size, clan.theme()))
            ),
            clickTo = text("view")
        ).build())) { click ->
            click.player.playClickSound()

            nautilus.scheduler.launch { ClanInterface(clan).create().open(click.player, click.view) }
        }
    }

    companion object {
        fun playOpenSound(player: Player) {
            player.playSound(player.location, Sound.UI_LOOM_SELECT_PATTERN, 10f, 2f)
        }
    }
}