package net.tjalp.nautilus.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import org.litote.kmongo.Id
import org.litote.kmongo.toId

/**
 * The global Gson helper, which helps in
 * all sorts of situations.
 */
object GsonHelper {

    /** The global [Gson] instance */
    private val gson: Gson = GsonBuilder().create()

    /** The global **pretty** [Gson] instance */
    private val prettyGson: Gson = GsonBuilder().setPrettyPrinting().create()

    /** The Mongo Gson parser */
    private val mongoGson: Gson = GsonBuilder()
        .registerTypeAdapter(
            Id::class.java,
            JsonSerializer<Id<Any>> { id, _, _ -> JsonPrimitive(id?.toString()) }
        )
        .registerTypeAdapter(
            Id::class.java,
            JsonDeserializer<Id<Any>> { id, _, _ -> id.asString.toId() }
        )
        .create()

    /**
     * Get the global [Gson] instance
     */
    fun global(): Gson = this.gson

    /**
     * Get the global **pretty** [Gson] instance,
     * for help with printing etc.
     */
    fun pretty(): Gson = this.prettyGson

    /**
     * Get the global Mongo data parser instance,
     * to (de)serialize data.
     */
    fun mongo(): Gson = this.mongoGson
}