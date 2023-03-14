package net.tjalp.nautilus.player.linking

import net.tjalp.nautilus.player.profile.ProfileSnapshot

interface LinkProvider<T> {

    fun isLinked(profile: ProfileSnapshot): Boolean

    suspend fun link(profile: ProfileSnapshot, link: T)

    fun link(profile: ProfileSnapshot): T?
}