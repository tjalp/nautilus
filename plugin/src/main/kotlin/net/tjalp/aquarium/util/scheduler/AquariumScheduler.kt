package net.tjalp.aquarium.util.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

object AquariumScheduler : CoroutineScope {

    override val coroutineContext: CoroutineContext = BukkitDispatcher + SupervisorJob()
}