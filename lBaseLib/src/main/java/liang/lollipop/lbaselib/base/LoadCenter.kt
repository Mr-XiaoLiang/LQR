package liang.lollipop.lbaselib.base

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar

/**
 * 加载中心
 * @author Lollipop
 * 加载动画的静态类
 */
object LoadCenter {

    var factory = object : Factory{
        override fun getLoader(): OnLoadCallback {
            return SimpleLoadCallback()
        }
    }

    fun getLoader(): OnLoadCallback{
        return factory.getLoader()
    }

    interface Factory{

        fun getLoader(): OnLoadCallback

    }

    private class SimpleLoadCallback: OnLoadCallback{

        override fun onBackPressed(): Boolean {
            if(loadTaskSize < 1){
                return true
            }
            onLoadEnd()
            return false
        }

        private var loadTaskSize = 0

        private var loaderGroup: FrameLayout? = null
        private var loaderView: ProgressBar? = null

        override fun onLoad(layoutInflater: LayoutInflater, viewGroup: ViewGroup) {

            if(loaderGroup == null){
                createView(viewGroup.context)
            }

            if(loaderGroup!!.parent == null){
                //创建一个添加到父容器中的布局参数
                val groupParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
                //将Loader容器添加到页面中
                viewGroup.addView(loaderGroup,groupParams)

                loaderView!!.isIndeterminate = true
            }

            loadTaskSize++

        }

        override fun onLoadEnd() {

            loadTaskSize--

            if(loadTaskSize < 1){

                loaderView?.isIndeterminate = false

                if(loaderGroup?.parent != null){

                    val group = loaderGroup!!.parent as ViewGroup

                    group.removeView(loaderGroup)

                }

            }

        }

        private fun createView(context: Context){
            //创建容器
            loaderGroup = FrameLayout(context)
            //为容器设置背景色，设置为50%透明度的黑色
            loaderGroup!!.setBackgroundColor(0x80000000.toInt())
            //捕获并且拦截全部手势操作
            loaderGroup!!.setOnTouchListener { _, _ -> true }

            //创建加载动画的View
            loaderView = ProgressBar(context)

            //创建布局属性为自适应，即默认大小的ProgressBar
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT)
            //设置位置属性为居中
            params.gravity = Gravity.CENTER

            //将ProgressBar及参数添加到容器中
            loaderGroup!!.addView(loaderView,params)

            //完成View创建动作

        }

    }

}