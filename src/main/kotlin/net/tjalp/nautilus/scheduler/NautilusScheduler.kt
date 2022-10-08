package net.tjalp.nautilus.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.tjalp.nautilus.Nautilus
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext

class NautilusScheduler(
    val nautilus: Nautilus
) : Executor, CoroutineScope {

    private val bukkitScheduler = this.nautilus.server.scheduler

    override val coroutineContext: CoroutineContext = BukkitScheduler + SupervisorJob()

    override fun execute(command: Runnable) {
        val schedule = this.bukkitScheduler.runTask(this.nautilus, Runnable {
            launch(Dispatchers.Unconfined) {
                command.run()
            }
        })
    }
}