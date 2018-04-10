package liang.lollipop.lbaselib.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * 加载动画的回调函数
 * 用于实现不同的加载动画
 * @author Lollipop
 */
interface OnLoadCallback {

    fun onLoad(layoutInflater: LayoutInflater, viewGroup: ViewGroup)

    fun onLoadEnd()

    fun onBackPressed(): Boolean

}