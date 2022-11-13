package net.tjalp.nautilus.util

import kotlin.math.floor

/**
 * A GroupedList represents a list of lists, split every X indexes.
 * Often used to create pages in command or containers.
 */
class GroupedList<T>(list: List<T>, split: Int) : Iterable<List<T>?> {

    private val pages: MutableList<MutableList<T>>

    constructor(iterable: Iterable<T>, split: Int) : this(iterable.toList(), split)

    /**
     * Returns the page count
     *
     * @return int
     */
    val groupCount: Int
        get() = pages.size

    /**
     * Return the amount of total entries
     *
     * @return int
     */
    val entryCount: Int
        get() = pages.stream()
            .mapToInt { obj: List<T> -> obj.size }
            .sum()

    /**
     * Returns the page at the given index
     *
     * @param index The page
     * @return List
     */
    fun getGroup(index: Int): List<T>? {
        if(index in 0..pages.lastIndex) {
            return pages[index]
        }

        return null
    }

    /**
     * Get the iterator for this object
     *
     */
    override fun iterator(): MutableIterator<MutableList<T>> {
        return pages.iterator()
    }

    init {
        check(split > 0) { "Split cannot be less than 1" }
        pages = ArrayList()

        for(i in list.indices) {
            val obj = list[i]

            if(i % split == 0) {
                pages.add(ArrayList())
            }

            val index = floor(i / split.toDouble()).toInt()
            pages[index].add(obj)
        }
    }
}