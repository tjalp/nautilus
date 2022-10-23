package net.tjalp.nautilus.util

import java.util.*
import java.util.stream.Collector

/**
 * Utility class used to create strings of lists separated
 * by delimiters and a final terminator
 */
class ListJoiner {

    private var value: MutableList<String>
    private var delimiter: String
    private var terminator: String

    constructor() {
        value = ArrayList()
        delimiter = ", "
        terminator = " and "
    }

    constructor(delimiter: String) {
        value = ArrayList()
        this.delimiter = delimiter
        terminator = " and "
    }

    constructor(delimiter: String, terminator: String) {
        value = ArrayList()
        this.delimiter = delimiter
        this.terminator = terminator
    }

    /**
     * Append the given string to this list joiner
     *
     * @param entry String entry
     * @return self
     */
    fun add(entry: String?): ListJoiner {
        if(entry == null) return this
        value.add(entry)
        return this
    }

    /**
     * Append the given strings to this list joiner
     *
     * @param entries String entries
     * @return self
     */
    fun addAll(entries: MutableCollection<String>): ListJoiner {
        entries.removeIf { obj: String? ->
            obj == null
        }
        value.addAll(entries)
        return this
    }

    /**
     * Append the given strings to this list joiner
     *
     * @param entries String entries
     * @return self
     */
    fun addAll(entries: Array<String?>): ListJoiner {
        value.addAll(Arrays.asList<String>(*entries))
        return this
    }

    /**
     * Returns the amount of elements in this joiner
     *
     * @return int
     */
    fun size(): Int {
        return value.size
    }

    /**
     * Create the string representation of this list
     *
     * @return String
     */
    override fun toString(): String {
        val size = value.size
        if(size == 0) return ""
        val builder = StringBuilder(value[0])

        if(size > 1) {
            for(i in 1 until size) {
                val pre: String = if(i == size - 1) {
                    terminator
                } else {
                    delimiter
                }

                builder.append(pre).append(value[i])
            }
        }

        return builder.toString()
    }

    companion object {

        fun collector(
            delimiter: String = ", ",
            terminator: String = " and "
        ): Collector<String, ListJoiner, String> {
            return Collector.of(
                { ListJoiner(delimiter, terminator) },
                { obj: ListJoiner, entry: String? ->
                    obj.add(entry)
                },
                { joiner1: ListJoiner, joiner2: ListJoiner ->
                    ListJoiner().addAll(joiner1.value).addAll(joiner2.value)
                },
                { obj: ListJoiner -> obj.toString() }
            )
        }

    }
}