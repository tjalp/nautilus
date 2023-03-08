package net.tjalp.nautilus.util

import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.floor

/**
 * Various math utility functions used on Rubicon
 *
 * @author Julian Mills
 */
object NautilusMath {

    const val EPSILON = 0.00001

    /**
     * Perform a linear interpolation from [a] to [b] at the
     * given percentage
     *
     * @param a Start
     * @param b End
     * @param percent Percentage
     * @return Value
     */
    fun lerp(a: Double, b: Double, percent: Double): Double {
        return (1.0 - percent) * a + percent * b
    }

    /**
     * Perform an inverse linear interpolation from [a] to [b] at the
     * given value, resulting in a percentage
     *
     * @param a Start
     * @param b End
     * @param value The value
     * @return Value
     */
    fun lerpInverse(a: Double, b: Double, value: Double): Double {
        return (value - a) / (b - a)
    }

    /**
     * Perform a linear interpolation between two colors
     * in the HSB color space.
     *
     * @param A The start color
     * @param B The end color
     * @param v The progress value
     */
    fun lerpColor(A: Color, B: Color, v: Double): Color {
        val target = v.coerceIn(0.0, 1.0)
        val from = Color.RGBtoHSB(A.red, A.green, A.blue, null)
        val to = Color.RGBtoHSB(B.red, B.green, B.blue, null)
        val delta = FloatArray(3)

        for(i in delta.indices) {
            delta[i] = lerp(from[i].toDouble(), to[i].toDouble(), target).toFloat()
        }

        return Color(Color.HSBtoRGB(delta[0], delta[1], delta[2]))
    }

    /**
     * Returns whether the given doubles are considered
     * approximately equal.
     *
     * @param a The first double
     * @param b The second double
     * @return The result
     */
    fun isEqualApprox(a: Double, b: Double) : Boolean {
        if(a == b) {
            return true
        }

        var tolerance = EPSILON * abs(a)

        if(tolerance < EPSILON) {
            tolerance = EPSILON
        }

        return abs(a - b) < tolerance
    }

    /**
     * Returns whether the given double is
     * considered approximately zero. This method
     * is faster than [isEqualApprox].
     *
     * @param n The double value
     * @return The result
     */
    fun isZeroApprox(n: Double) : Boolean {
        return abs(n) < EPSILON
    }

    /**
     * Function that blends two colors together
     *
     * @param c1 Color one
     * @param c2 Color two
     * @param ratio Ratio (0 - 1)
     * @return Blended color
     */
    fun mixColor(c1: Color, c2: Color, ratio: Float): Color {
        var theRatio = ratio.coerceIn(0f, 1f)

        if(theRatio > 1f) {
            theRatio = 1f
        } else if(theRatio < 0f) {
            theRatio = 0f
        }

        val iRatio = 1.0f - theRatio

        val i1 = c1.rgb
        val i2 = c2.rgb

        val a1 = i1 shr 24 and 0xff
        val r1 = i1 and 0xff0000 shr 16
        val g1 = i1 and 0xff00 shr 8
        val b1 = i1 and 0xff

        val a2 = i2 shr 24 and 0xff
        val r2 = i2 and 0xff0000 shr 16
        val g2 = i2 and 0xff00 shr 8
        val b2 = i2 and 0xff

        val a = (a1 * iRatio + a2 * theRatio).toInt()
        val r = (r1 * iRatio + r2 * theRatio).toInt()
        val g = (g1 * iRatio + g2 * theRatio).toInt()
        val b = (b1 * iRatio + b2 * theRatio).toInt()

        return Color(a shl 24 or (r shl 16) or (g shl 8) or b)
    }

    /**
     * Returns the avarage color of the specified image.
     *
     * @param image The image
     * @return The color
     */
    fun getAvarageColor(image: BufferedImage) : Color {
        var redBucket = 0
        var greenBucket = 0
        var blueBucket = 0
        var total = 0

        for(x in 0 until image.width) {
            for(y in 0 until image.height) {
                val color = Color(image.getRGB(x, y))

                total++
                redBucket += color.red
                greenBucket += color.green
                blueBucket += color.blue
            }
        }

        return Color(redBucket / total, greenBucket / total, blueBucket / total)
    }

    /**
     * Utility used to phase between different color stops given a tick
     *
     * Copyright 2019-2022 (c) Starlane Studios. All Rights Reserved.
     *
     * @author Julian Mills
     */
    class ColorPhaser(
        val frequency: Int,
        vararg val stops: Color
    ) {

        /**
         * Calculate the next color based on the current tick
         *
         * @param tick The current tick
         * @return The Color
         */
        fun next(tick: Long): Color {
            val freq = frequency.toFloat()
            val previous = stops[Math.floorMod(floor((tick - freq) / freq).toInt(), stops.size)]
            val next = stops[Math.floorMod(floor(tick / freq).toInt(), stops.size)]
            val progress = tick % freq / freq

            return mixColor(previous, next, progress)
        }

    }

}