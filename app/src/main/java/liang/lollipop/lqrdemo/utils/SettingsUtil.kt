package liang.lollipop.lqrdemo.utils

import android.content.Context
import liang.lollipop.lbaselib.util.PreferencesUtil

/**
 * 设置工具类
 * @author Lollipop
 */
object SettingsUtil {

    private const val KEY_DARK_COLOR = "KEY_DARK_COLOR"

    private const val KEY_LIGHT_COLOR = "KEY_LIGHT_COLOR"

    private const val KEY_MINI_MODEL = "KEY_MINI_MODEL"

    private const val KEY_CAMERA_MODEL = "KEY_CAMERA_MODEL"

    const val CAMERA_MODEL_AUTO = 0
    const val CAMERA_MODEL_OLD = 1
    const val CAMERA_MODEL_NEW = 2

    fun <T> get(context: Context,key: String,def:T): T{
        return PreferencesUtil.get(context,key,def)
    }

    fun <T> put(context: Context,key: String,value:T){
        PreferencesUtil.put(context,key,value)
    }

    fun getDarkColor(context: Context): Int{
        return get(context,KEY_DARK_COLOR,0xFF000000.toInt())
    }

    fun putDarkColor(context: Context,value: Int){
        put(context,KEY_DARK_COLOR,value)
    }

    fun getLightColor(context: Context): Int{
        return get(context,KEY_LIGHT_COLOR,0xFFFFFFFF.toInt())
    }

    fun putLightColor(context: Context,value: Int){
        put(context,KEY_LIGHT_COLOR,value)
    }

    fun isMiniModel(context: Context): Boolean{
        return get(context, KEY_MINI_MODEL,false)
    }

    fun setMiniModel(context: Context,value: Boolean){
        put(context, KEY_MINI_MODEL,value)
    }

    fun getCameraModel(context: Context): Int{
        return get(context,KEY_CAMERA_MODEL, CAMERA_MODEL_AUTO)
    }

    fun setCameraModel(context: Context,value: Int){
        put(context,KEY_CAMERA_MODEL,value)
    }

}
