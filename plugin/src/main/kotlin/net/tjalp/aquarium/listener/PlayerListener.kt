package net.tjalp.aquarium.listener

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import me.neznamy.tab.api.event.Subscribe
import me.neznamy.tab.api.event.player.PlayerLoadEvent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.registry.DECORATED_CHAT
import net.tjalp.aquarium.util.getChatColor
import net.tjalp.aquarium.util.getFormattedName
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerChatPreviewEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Suppress("UNUSED")
class PlayerListener : Listener {

    private val tags = Aquarium.nametagManager
    private val lp = Aquarium.luckperms

    init {
        val lpBus = lp.eventBus

        tags.tabApi.eventBus.register(this)
        lpBus.subscribe(Aquarium.loader, UserDataRecalculateEvent::class.java, this::onUserDataRecalculate)
    }

    @Subscribe
    fun onPlayerLoad(event: PlayerLoadEvent) {
        val player = event.player
        val bukkitPlayer = player.player as Player

        tags.update(bukkitPlayer)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val display = player.getFormattedName()

        player.displayName(display)
        player.playerListName(display)
        event.joinMessage(translatable("multiplayer.player.joined", display))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        event.quitMessage(translatable("multiplayer.player.left", player.displayName()))
    }

    @EventHandler
    fun onAsyncChatDecorate(event: AsyncChatDecorateEvent) {
        val player = event.player() ?: return
        val mini = miniMessage()
        val chatColor = player.getChatColor()
        val text = event.result() as TextComponent

        if (!player.hasPermission(DECORATED_CHAT)) {
            event.result(text().append(mini.deserialize(chatColor + mini.escapeTags(text.content()))).build())
            return
        }

        event.result(mini.deserialize(chatColor + text.content()))
    }

    @EventHandler
    fun onAsyncPlayerChatPreview(event: AsyncPlayerChatPreviewEvent) {
        val legacy = LegacyComponentSerializer.legacySection()
        val mini = miniMessage()
        val fullName = event.player.getFormattedName(useSuffix = false)

        event.format = legacy.serialize(fullName.append(mini.deserialize(" <#82826f>→ <reset>%2\$s")))
    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        val legacy = LegacyComponentSerializer.legacySection()
        val mini = miniMessage()
        val fullName = event.player.getFormattedName(useSuffix = false)

        event.format = legacy.serialize(fullName.append(mini.deserialize(" <#82826f>→ <reset>%2\$s")))
    }

    private fun onUserDataRecalculate(event: UserDataRecalculateEvent) {
        val user = event.user
        val player = Aquarium.loader.server.getPlayer(user.uniqueId) ?: return
        val display = player.getFormattedName()

        tags.update(player)
        player.displayName(display)
        player.playerListName(display)
    }

//    @EventHandler
//    private fun onPlayerJoin(event: PlayerJoinEvent) {
//        val player = event.player
//        val loc = player.location
//        val name = MiniMessage.miniMessage().deserialize("<rainbow>${player.name}</rainbow>")
//        player.playerListName(name)
//        val stand = (player.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand).apply {
//            isCustomNameVisible = true
//            customName(name)
//            isInvisible = true
//            isMarker = true
//        }
//        Aquarium.instance.server.scheduler.runTaskTimer(Aquarium.instance, { task ->
//            if (!player.isOnline) {
//                task.cancel()
//                stand.remove()
//                return@runTaskTimer
//            }
//            stand.teleport(player.eyeLocation.add(0.0, 0.085, 0.0))
//            stand.isCustomNameVisible = !player.isSneaking
//        }, 0L, 1L)
//
//        player.asNms().networkManager.channel.pipeline().addBefore("packet_handler", "injector", PacketHandler(player))
//    }
//
//    @EventHandler
//    private fun onPlayerQuit(event: PlayerQuitEvent) {
//        event.player.passengers.forEach {
//            if (it is ArmorStand) it.remove()
//        }
//    }
//
//    inner class PacketHandler(private val player: Player) : ChannelDuplexHandler() {
//
//        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
//            if (msg !is ServerboundPlayerActionPacket) {
//                super.channelRead(ctx, msg)
//                return
//            }
//
//            if (msg.action == START_DESTROY_BLOCK) Aquarium.instance.server.broadcast(
//                Component.text("Start digging block"))
//
//            val block = msg.pos.asBukkit(player.world).block
//
//            when (msg.action) {
//                START_DESTROY_BLOCK -> {
//                    if (block.type.hardness <= 0.0 || (block.state as CraftBlockState).handle.getDestroyProgress(player.asNms(), player.asNms().level, block.location.asNms()) > 1.0F) {
//                        Aquarium.instance.server.broadcast(Component.text("Insta mine"))
//                        super.channelRead(ctx, msg)
//                        return
//                    }
//                    Aquarium.instance.digManager.startDigging(player, block)
//                }
//                ABORT_DESTROY_BLOCK -> Aquarium.instance.digManager.stopDigging(player, block)
//                else -> {}
//            }
//        }
//
//        override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
//            if (msg is ClientboundSetEntityDataPacket) {
//                msg.unpackedData?.forEach {
//                    val value = it.value
//                    if (value is Pose && value == Pose.FALL_FLYING) {
//                        it.value = Pose.SPIN_ATTACK
//                    }
//                }
//                msg.unpackedData?.set(18, SynchedEntityData.DataItem(EntityDataSerializers.POSE.createAccessor(msg.id), net.minecraft.world.entity.Pose.SPIN_ATTACK))
//            }
//
//            super.write(ctx, msg, promise)
//        }
//    }
}