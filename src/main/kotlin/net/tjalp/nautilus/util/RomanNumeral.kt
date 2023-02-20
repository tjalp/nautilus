package net.tjalp.nautilus.util

import java.util.*

/**
 * Utility class for converting numbers to roman numerals.
 */
object RomanNumeral {
    private val map = TreeMap<Int, String>()

    init {
        map[1000] = "M"
        map[900] = "CM"
        map[500] = "D"
        map[400] = "CD"
        map[100] = "C"
        map[90] = "XC"
        map[50] = "L"
        map[40] = "XL"
        map[10] = "X"
        map[9] = "IX"
        map[5] = "V"
        map[4] = "IV"
        map[1] = "I"
    }

    /**
     * Converts a number to a roman numeral.
     *
     * @param number The number to convert.
     * @return The roman numeral.
     */
    fun toRoman(number: Int): String? {
        val l = map.floorKey(number)
        return if (number == l) {
            map[number]
        } else map[l] + toRoman(number - l)
    }
}