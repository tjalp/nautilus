package net.tjalp.aquarium.manager

import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.tjalp.aquarium.Aquarium
import net.tjalp.aquarium.util.asNms
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockState
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import kotlin.math.floor

class DigManager {

    private val miningStartTime = hashMapOf<Player, Long>()
    private val tasks = hashMapOf<Player, MutableList<BukkitTask>>()

    fun startDigging(player: Player, block: Block) {
        val nmsItem = player.asNms().mainHandItem
        val isInstabreak = block.type.hardness <= 0.0 || (block.state as CraftBlockState).handle.getDestroyProgress(player.asNms(), player.asNms().level, block.location.asNms()) > 1.0F
//                || nmsItem.item.mineBlock(
//            nmsItem,
//            player.asNms().level,
//            (block.state as CraftBlockState).handle,
//            block.location.asNms(),
//            player.asNms()
//        )

        if (isInstabreak) Bukkit.broadcast(Component.text("isInstabreak"))

        //Bukkit.broadcast(Component.text("Starting timer"))
        miningStartTime[player] = System.currentTimeMillis()

        player.asNms().networkManager.send(ClientboundBlockDestructionPacket(player.entityId, block.location.asNms(), 10))

        val task = Aquarium.instance.server.scheduler.runTaskTimer(Aquarium.instance, Runnable {
            val stageTime = 2L
            val ticks = (System.currentTimeMillis() - miningStartTime[player]!!) / 200 / stageTime

            if (floor(ticks.toDouble()).toInt() >= 11 || isInstabreak) {
                stopDigging(player, block)
                if (!isInstabreak) block.world.playEffect(block.location, Effect.STEP_SOUND, block.type)
                block.breakNaturally(player.inventory.itemInMainHand, false)
                return@Runnable
            }

            //Bukkit.broadcast(Component.text(ticks))
            player.asNms().networkManager.send(ClientboundBlockDestructionPacket(player.entityId + 1, block.location.asNms(), floor(ticks.toDouble()).toInt() - 1))
        }, 0L, 1L)

        val list = tasks[player]
        if (list != null) list.add(task) else tasks[player] = mutableListOf(task)

        if (!isInstabreak) player.asNms().networkManager.send(ClientboundUpdateMobEffectPacket(player.entityId, MobEffectInstance(MobEffects.DIG_SLOWDOWN, Int.MAX_VALUE, 3, false, false)))
    }

    fun stopDigging(player: Player, block: Block) {
        //Bukkit.broadcast(Component.text("Stopping timers"))
        tasks[player]?.forEach { it.cancel() }
        player.asNms().networkManager.send(ClientboundBlockDestructionPacket(player.entityId + 1, block.location.asNms(), -1))

        if (!player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
            player.asNms().networkManager.send(ClientboundRemoveMobEffectPacket(player.entityId, MobEffects.DIG_SLOWDOWN))
        }
    }
}