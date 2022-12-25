package net.tjalp.nautilus.player.teleport

interface TeleportRequest {

    fun request()

    fun accept()

    fun deny()

    fun cancel()
}