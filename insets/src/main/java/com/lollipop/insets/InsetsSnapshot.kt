package com.lollipop.insets

import androidx.core.graphics.Insets
import kotlin.math.max
import kotlin.math.min

class BoundsSnapshot(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    companion object {
        val EMPTY = BoundsSnapshot(0, 0, 0, 0)
    }

    fun minOf(insets: Insets): Insets {
        return Insets.of(
            min(left, insets.left),
            min(top, insets.top),
            min(right, insets.right),
            min(bottom, insets.bottom)
        )
    }

    fun maxOf(insets: Insets): Insets {
        return Insets.of(
            max(left, insets.left),
            max(top, insets.top),
            max(right, insets.right),
            max(bottom, insets.bottom)
        )
    }

    fun leftAtMost(maximumValue: Int): Int {
        return left.coerceAtMost(maximumValue)
    }

    fun topAtMost(maximumValue: Int): Int {
        return top.coerceAtMost(maximumValue)
    }

    fun rightAtMost(maximumValue: Int): Int {
        return right.coerceAtMost(maximumValue)
    }

    fun bottomAtMost(maximumValue: Int): Int {
        return bottom.coerceAtMost(maximumValue)
    }

    fun leftAtLeast(minimumValue: Int): Int {
        return left.coerceAtLeast(minimumValue)
    }

    fun topAtLeast(minimumValue: Int): Int {
        return top.coerceAtLeast(minimumValue)
    }

    fun rightAtLeast(minimumValue: Int): Int {
        return right.coerceAtLeast(minimumValue)
    }

    fun bottomAtLeast(minimumValue: Int): Int {
        return bottom.coerceAtLeast(minimumValue)
    }

}

class InsetsSnapshot(
    val margin: BoundsSnapshot,
    val padding: BoundsSnapshot,
)
