package com.lollipop.insets

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.max
import kotlin.math.min

object LInsets {

    fun fitsSystemWindows(activity: Activity) {
        if (activity is ComponentActivity) {
            activity.enableEdgeToEdge()
        } else {
            fitsSystemWindows(activity.window)
        }
    }

    fun fitsSystemWindows(window: Window) {
        window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    fun getController(activity: Activity): WindowInsetsControllerCompat {
        return getController(activity.window)
    }

    fun getController(window: Window): WindowInsetsControllerCompat {
        return WindowCompat.getInsetsController(window, window.decorView)
    }

    fun maxOf(insets: WindowInsetsCompat, vararg types: WindowInsetsType): Insets {
        val insetsArray = types.map { it -> insets.getInsets(it.typeMask()) }
        return maxOf(insetsArray)
    }

    fun minOf(insets: WindowInsetsCompat, vararg types: WindowInsetsType): Insets {
        val insetsArray = types.map { it -> insets.getInsets(it.typeMask()) }
        return minOf(insetsArray)
    }

    fun minOf(insets: List<Insets>): Insets {
        if (insets.isEmpty()) {
            return Insets.NONE
        }
        val minRect = Rect(insets[0].left, insets[0].top, insets[0].right, insets[0].bottom)
        if (insets.size > 1) {
            for (i in 1 until insets.size) {
                val inset = insets[i]
                minRect.left = min(minRect.left, inset.left)
                minRect.top = min(minRect.top, inset.top)
                minRect.right = min(minRect.right, inset.right)
                minRect.bottom = min(minRect.bottom, inset.bottom)
            }
        }
        return Insets.of(minRect)
    }

    fun maxOf(insets: List<Insets>): Insets {
        if (insets.isEmpty()) {
            return Insets.NONE
        }
        val maxRect = Rect(insets[0].left, insets[0].top, insets[0].right, insets[0].bottom)
        if (insets.size > 1) {
            for (i in 1 until insets.size) {
                val inset = insets[i]
                maxRect.left = max(maxRect.left, inset.left)
                maxRect.top = max(maxRect.top, inset.top)
                maxRect.right = max(maxRect.right, inset.right)
                maxRect.bottom = max(maxRect.bottom, inset.bottom)
            }
        }
        return Insets.of(maxRect)
    }

}

fun Dialog.enableEdgeToEdge() {
    window?.let {
        LInsets.fitsSystemWindows(it)
    }
}

fun View.applyWindowInsets(
    callback: (view: View, snapshot: InsetsSnapshot, insets: WindowInsetsCompat) -> Unit
) {
    val insetsSnapshot = InsetsSnapshot(
        padding = snapshotPaddingInner(this),
        margin = snapshotMarginInner(this)
    )
    ViewCompat.setOnApplyWindowInsetsListener(
        this
    ) { v, insets ->
        callback(v, insetsSnapshot, insets)
        insets
    }
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        post {
            requestApplyInsets()
        }
    }
}

private fun snapshotPaddingInner(target: View): BoundsSnapshot {
    return BoundsSnapshot(
        left = target.paddingLeft,
        top = target.paddingTop,
        right = target.paddingRight,
        bottom = target.paddingBottom
    )
}

private fun snapshotMarginInner(target: View): BoundsSnapshot {
    val layoutParams = target.layoutParams ?: return BoundsSnapshot.EMPTY
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        return BoundsSnapshot(
            left = layoutParams.leftMargin,
            top = layoutParams.topMargin,
            right = layoutParams.rightMargin,
            bottom = layoutParams.bottomMargin
        )
    }
    return BoundsSnapshot.EMPTY
}