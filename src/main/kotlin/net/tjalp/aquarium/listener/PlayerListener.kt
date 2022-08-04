package net.tjalp.aquarium.listener

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import me.neznamy.tab.api.event.Subscribe
import me.neznamy.tab.api.event.player.PlayerLoadEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.registry.DECORATED_CHAT
import net.tjalp.aquarium.util.getPrefix
import net.tjalp.aquarium.util.getSuffix
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@Suppress("UNUSED")
class PlayerListener(
    private val aquarium: Aquarium
) : Listener {

    private val tags = aquarium.nametagManager
    private val lp = aquarium.luckperms

    init {
        val lpBus = lp.eventBus

        tags.tabApi.eventBus.register(this)
        lpBus.subscribe(aquarium, UserDataRecalculateEvent::class.java, this::onUserDataRecalculate)
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
        val mini = MiniMessage.miniMessage()
        val prefix = mini.deserialize(player.getPrefix() ?: "")
        val suffix = mini.deserialize(player.getSuffix() ?: "")
        val display = Component.text().append(prefix).append(player.name()).append(suffix).build()

        player.displayName(display)
        player.playerListName(display)
    }

    @EventHandler
    fun onAsyncChatDecorate(event: AsyncChatDecorateEvent) {
        val player = event.player() ?: return

        if (!player.hasPermission(DECORATED_CHAT)) return

        event.result(MiniMessage.miniMessage().deserialize((event.result() as TextComponent).content()))
    }

    private fun onUserDataRecalculate(event: UserDataRecalculateEvent) {
        val user = event.user
        val player = aquarium.server.getPlayer(user.uniqueId) ?: return
        val mini = MiniMessage.miniMessage()
        val prefix = mini.deserialize(user.cachedData.metaData.prefix ?: "")
        val suffix = mini.deserialize(user.cachedData.metaData.suffix ?: "")
        val display = Component.text().append(prefix).append(player.name()).append(suffix).build()

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
//            if (msg !is ClientboundBlockDestructionPacket) {
//                super.write(ctx, msg, promise)
//                return
//            }
//
//            super.write(ctx, msg, promise)
//        }
//    }
}