package net.tjalp.nautilus.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.Material

object ItemGenerator {

    fun clickable(material: Material, name: Component, description: Component, clickTo: Component): ItemBuilder {
        return this.clickable(material, name, clickTo, *arrayOf(description))
    }

    fun clickable(material: Material, name: Component, clickTo: Component, vararg description: Component) : ItemBuilder {
        val list = mutableListOf<TextComponent>()
        if (description.isNotEmpty()) list += empty()
        for (comp in description) list += text().color(color(251, 228, 96)).append(comp).build()
        val lore = arrayOf(
            *list.toTypedArray(),
            empty(),
            text().color(color(233, 210, 130))
                .append(text("\u2620"))
                .append(text(" \u2192 ", DARK_GRAY))
                .append(text("Click to "))
                .append(text().color(color(251, 228, 96)).append(clickTo))
                .build()
        )

        return ItemBuilder(material)
            .name(name.colorIfAbsent(WHITE))
            .lore(*lore)
    }
}