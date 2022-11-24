package net.tjalp.nautilus.util

import java.util.UUID

/**
 * Whether a String is a [UUID]
 *
 * @return true if unique id
 */
fun String.isUniqueId(): Boolean {
    return try {
        UUID.fromString(this)
        true
    } catch (ex: IllegalArgumentException) {
        false
    }
}