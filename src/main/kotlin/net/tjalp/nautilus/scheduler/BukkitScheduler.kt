package net.tjalp.nautilus.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import net.tjalp.nautilus.Nautilus
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine dispatcher to execute coroutines
 * on Bukkit's main thread.
 */
object BukkitScheduler : CoroutineDispatcher() {

    private val nautilus = Nautilus.get()

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !this.nautilus.server.isPrimaryThread
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        this.nautilus.server.scheduler.runTask(this.nautilus, block)
    }
}