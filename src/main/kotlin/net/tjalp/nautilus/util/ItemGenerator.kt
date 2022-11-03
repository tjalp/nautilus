package net.tjalp.nautilus.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material

object ItemGenerator {

    fun clickable(material: Material, name: Component, description: Component, clickTo: Component): ItemBuilder {
        val lore = arrayOf(
            empty(),
            text().color(YELLOW).append(description).build(),
            empty(),
            text().color(GREEN)
                .append(text("\u2620"))
                .append(text(" > ", DARK_GRAY))
                .append(text("Click to "))
                .append(text().color(DARK_GREEN).append(clickTo))
                .build()
        )

        return ItemBuilder(material)
            .name(name)
            .lore(*lore)
    }
}