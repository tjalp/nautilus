package net.tjalp.nautilus.world.claim

import java.util.UUID

data class WorldChunkMap(
    val world: UUID,
    val chunks: Set<Long> = emptySet()
)
