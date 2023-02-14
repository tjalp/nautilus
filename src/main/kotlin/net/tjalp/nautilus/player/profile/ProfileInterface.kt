package net.tjalp.nautilus.player.profile

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor.color
import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.clan.ClanInterface
import net.tjalp.nautilus.clan.ClanSnapshot
import net.tjalp.nautilus.interfaces.NautilusInterface
import net.tjalp.nautilus.player.teleport.PlayerTeleportRequest
import net.tjalp.nautilus.util.*
import net.tjalp.nautilus.util.ItemGenerator.clickable
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.geysermc.cumulus.form.Form
import org.geysermc.cumulus.form.SimpleForm
import org.incendo.interfaces.core.Interface
import org.incendo.interfaces.core.view.InterfaceView
import org.incendo.interfaces.kotlin.paper.asElement
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.pane.ChestPane

class ProfileInterface(
    private val profile: ProfileSnapshot
) : NautilusInterface<ChestPane>() {

    private val nautilus = Nautilus.get()
    private var clan: ClanSnapshot? = profile.clan()
    private var isClanLoading = clan == null

    init {
        if (clan == null && profile.clanId != null) {
            this.nautilus.scheduler.launch {
                clan = nautilus.clans.clan(profile.clanId)
                isClanLoading = false
            }
        }
    }

    override fun `interface`(): Interface<ChestPane, PlayerViewer> {
        return buildChestInterface {
            title = text()
                .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false, showHover = false, isClickable = false))
                .append(text("'s profile"))
                .build()
            rows = 4

            withTransform { view ->
                val lastOnline = if (profile.player()?.isOnline == true) {
                    text("Currently online")
                } else {
                    text("Last online ${Nautilus.TIME_FORMAT.format(profile.lastOnline)}")
                }

                view[4, 0] = ItemBuilder(Material.PLAYER_HEAD)
                    .skull(profile.lastKnownSkin)
                    .name(profile.nameComponent(useMask = false, showSuffix = false, showHover = false, isClickable = false))
                    .lore(
                        empty(),
                        text("• First joined ${Nautilus.TIME_FORMAT.format(profile.firstJoin)}", color(251, 228, 96)),
                        text("• ", color(251, 228, 96)).append(lastOnline)
                    )
                    .build().asElement()

                view[3, 2] = clickable(
                    material = Material.ENDER_PEARL,
                    name = text("Teleport Request"),
                    description = arrayOf(
                        text("Send a request to teleport to this player"),
                        empty(),
                        text("Teleport requests must be accepted by"),
                        text("the target player in order to teleport")
                    ),
                    clickTo = text("send a Teleport Request")
                ).build().asElement { click ->
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
                        return@asElement
                    }

                    PlayerTeleportRequest(player, target).request()
                }

                view.addTask(nautilus, {
                    nautilus.scheduler.launch {
                        val clan = nautilus.clans.clan(profile.clanId ?: return@launch) ?: return@launch

                        view[5, 2] = clickable(
                            material = Material.TOTEM_OF_UNDYING,
                            name = text("Clan"),
                            description = text("• Name: ").append(text(clan.name, clan.theme())),
                            clickTo = text("view")
                        ).build().asElement { click ->
                            val player = click.viewer().player()

                            player.playClickSound()

                            ClanInterface(clan, playSound = false).parent(this@ProfileInterface).open(player)
                        }
                    }
                }, 0)

                returnIcon()?.let { view[0, 3] = it }
            }
        }
    }

    override fun form(viewer: Player): Form {
        return SimpleForm.builder()
            .title("${profile.displayName()}'s profile")
            .button("Send Teleport Request") // id 0
            .validResultHandler { response ->
                when (response.clickedButtonId()) {
                    0 -> {
                        val target = this@ProfileInterface.profile.player()

                        if (target == null || profile.maskName != null) {
                            viewer.sendMessage(
                                text().color(RED)
                                    .append(profile.nameComponent(useMask = false, showPrefix = false, showSuffix = false))
                                    .appendSpace().append(text("is not online at the moment"))
                            )
                            return@validResultHandler
                        }

                        PlayerTeleportRequest(viewer, target).request()
                    }
                }
            }
            .build()
    }

    fun openWithSound(viewer: PlayerViewer): InterfaceView<ChestPane, PlayerViewer> {
        val player = viewer.player()
        player.playSound(player.location, Sound.UI_LOOM_SELECT_PATTERN, 10f, 2f)
        return this.open(viewer)
    }
}