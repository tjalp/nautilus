package net.tjalp.aquarium.util.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import net.tjalp.aquarium.Aquarium
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

/**
 * Specialized coroutine dispatcher used to execute
 * coroutines on the Bukkit main thread.
 *
 * @author Julian Mills
 */
object BukkitDispatcher : CoroutineDispatcher() {

    private val aquarium = Aquarium.loader

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Bukkit.isPrimaryThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTask(aquarium, block)
    }

}