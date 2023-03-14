package net.tjalp.aquarium.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun mini(value: String): Component {
    return MiniMessage.miniMessage().deserialize(value)
}