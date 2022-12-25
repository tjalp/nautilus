package net.tjalp.nautilus.registry

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.util.displayName
import net.tjalp.nautilus.util.profile

val RANK_SUGGESTIONS
    get() = Nautilus.get().commands.parserRegistry().getSuggestionProvider("ranks").get()

val DISPLAY_NAME_SUGGESTIONS
    get() = Nautilus.get().commands.parserRegistry().getSuggestionProvider("display_names").get()

val REAL_NAME_SUGGESTIONS
    get() = Nautilus.get().commands.parserRegistry().getSuggestionProvider("real_names").get()

/**
 * Register all global command suggestions
 */
fun registerSuggestions(nautilus: Nautilus) {
    val registry = nautilus.commands.parserRegistry()

    registry.registerSuggestionProvider("ranks") { _, input ->
        nautilus.perms.ranks
            .filter { it.id.startsWith(input, ignoreCase = true) }
            .map { it.id }
    }

    registry.registerSuggestionProvider("display_names") { _, input ->
        nautilus.server.onlinePlayers
            .map { it.profile().displayName() }
            .filter { it.startsWith(input, ignoreCase = true) }
    }

    registry.registerSuggestionProvider("real_names") { _, input ->
        nautilus.server.onlinePlayers
            .map { it.name }
            .filter { it.startsWith(input, ignoreCase = true) }
    }
}