package net.tjalp.nautilus.registry

import net.tjalp.nautilus.Nautilus

val RANK_SUGGESTIONS
    get() = Nautilus.get().commands.parserRegistry().getSuggestionProvider("ranks").get()

/**
 * Register all global command suggestions
 */
fun registerSuggestions(nautilus: Nautilus) {
    val registry = nautilus.commands.parserRegistry()

    registry.registerSuggestionProvider("ranks") { _, input ->
        nautilus.perms.ranks
            .filter { it.id.startsWith(input, true) }
            .map { it.id }
    }
}