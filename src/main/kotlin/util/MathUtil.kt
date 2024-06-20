package org.example.util

object MathUtil {
    fun round(num : Float) : Float {
        return (num * 100).toInt().toFloat() / 100
    }

    fun toPercent(numerator: Int, denominator: Int): Float {
        return (numerator.toFloat() / denominator.toFloat() * 100 /*to percent*/)
    }

    fun roundPercent(numerator: Int, denominator: Int) = round(toPercent(numerator, denominator))
}