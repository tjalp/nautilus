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
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement
import org.incendo.interfaces.paper.pane.ChestPane

class ProfileInterface(
    private val parent: Interface<*, PlayerViewer>?,
    private val nautilus: Nautilus,
    val profile: ProfileSnapshot
) : NautilusInterface<ChestPane> {

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
    private val loadingClanElement: ItemStackElement<ChestPane>; get() {
        return ItemStackElement(clickable(
            material = Material.TOTEM_OF_UNDYING,
            name = text("Clan"),
            description = text("Loading..."),
            clickTo = text("do nothing")
        ).build())
    }

    override fun create() = buildChestInterface {
        title = text()
            .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false, showHover = false, isClickable = false))
            .append(text("'s profile"))
            .build()
        rows = 4

        withTransform { view ->
            this@ProfileInterface.nautilus.scheduler.launch {
                view[5, 2] = clanElement()
            }
        }

        withTransform { view ->
            view[4, 0] = ItemStackElement(skull)
            view[3, 2] = teleportElement()
            view[5, 2] = loadingClanElement
            if (parent() != null) view[0, 3] = backElement()
        }

    }

    override fun parent() = this.parent

    private fun teleportElement(): ItemStackElement<ChestPane> {
        return ItemStackElement(clickable(
            material = Material.ENDER_PEARL,
            name = text("Teleport Request"),
            description = arrayOf(
                text("Send a request to teleport to this player"),
                empty(),
                text("Teleport requests must be accepted by"),
                text("the target player in order to teleport")
            ),
            clickTo = text("send a Teleport Request")
        ).build()) { click ->
            val player = click.viewer().player()
            val target = this@ProfileInterface.profile.player()

            click.viewer().close()
            player.playClickSound()

            if (target == null || profile.maskName != null) {
                player.sendMessage(
                    text().color(RED)
                        .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                        .appendSpace().append(text("is not online at the moment"))
                )
                return@ItemStackElement
            }

            PlayerTeleportRequest(player, target).request()
        }
    }

    private suspend fun clanElement(): ItemStackElement<ChestPane> {
        val clan = profile.clanId?.let { this.nautilus.clans.clan(it) } ?: return ItemStackElement(clickable(
            material = Material.TOTEM_OF_UNDYING,
            name = text("Clan"),
            description = text("This player is not in a clan"),
            clickTo = text("do nothing")
        ).build())

        return ItemStackElement(clickable(
            material = Material.TOTEM_OF_UNDYING,
            name = text("Clan"),
            description = arrayOf(
                text("• Name: ").append(text(clan.name, clan.theme())),
                text("• Leaders: ").append(text(clan.leaders.size, clan.theme())),
                text("• Members: ").append(text(clan.members.size, clan.theme()))
            ),
            clickTo = text("view")
        ).build()) { click ->
            click.viewer().player().playClickSound()

            nautilus.scheduler.launch { ClanInterface(click.view().backing(), clan).create().open(click.viewer()) }
        }
    }

    companion object {
        fun playOpenSound(player: Player) {
            player.playSound(player.location, Sound.UI_LOOM_SELECT_PATTERN, 10f, 2f)
        }
    }
}