package net.tjalp.nautilus.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.Material

object ItemGenerator {

    fun clickable(material: Material, name: Component, description: Component, clickTo: Component): ItemBuilder {
        val lore = arrayOf(
            empty(),
            text().color(color(251, 228, 96)).append(description).build(),
            empty(),
            text().color(color(233, 210, 130))
                .append(text("\u2620"))
                .append(text(" > ", DARK_GRAY))
                .append(text("Click to "))
                .append(text().color(color(251, 228, 96)).append(clickTo))
                .build()
        )

        return ItemBuilder(material)
            .name(name)
            .lore(*lore)
    }
}