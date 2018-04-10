package liang.lollipop.lqr.view

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.google.zxing.ResultPoint
import liang.lollipop.lqr.util.QRConstant

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 二维码扫描的提示框View叠加层
 */
open class QRFinderView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : View(context, attrs, defStyleAttr),QRFinder{

    constructor(context: Context,attrs: AttributeSet?): this(context,attrs,0)
    constructor(context: Context): this(context,null)

    private val finderDrawable = QRFinderDrawable()

    private var isDetached = false

    /**
     * 蒙板的颜色，默认颜色取值为50%透明度的黑色(#80000000)
     */
    var maskColor: Int
        get() = finderDrawable.maskColor
        set(value) {
            finderDrawable.maskColor = value
            postInvalidate()
        }

    /**
     * 边框的颜色，取色为绿色
     */
    var borderColor: Int
        get() = finderDrawable.borderColor
        set(value) {
            finderDrawable.borderColor = value
            postInvalidate()
        }

    /**
     * 扫描线的颜色
     */
    var scanLineColor: Int
        get() = finderDrawable.scanLineColor
        set(value) {
            finderDrawable.scanLineColor = value
            postInvalidate()
        }

    /**
     * 边框的宽度
     */
    var borderWidth: Float
        get() = finderDrawable.borderWidth
        set(value) {
            finderDrawable.borderWidth = value
            postInvalidate()
        }

    /**
     * 可能点的颜色，用于绘制扫描框内的小点
     */
    var pointColor: Int
        get() = finderDrawable.pointColor
        set(value) {
            finderDrawable.pointColor = value
            postInvalidate()
        }

    /**
     * 小点的尺寸
     */
    var pointRadius: Float
        get() = finderDrawable.pointRadius
        set(value) {
            finderDrawable.pointRadius = value
            postInvalidate()
        }

    /**
     * 小号的识别点半径
     */
    var miniPointRadius: Float
        get() = finderDrawable.miniPointRadius
        set(value) {
            finderDrawable.miniPointRadius = value
            postInvalidate()
        }

    /**
     * 扫描框高度
     */
    var scanLineHeight: Int
        get() = finderDrawable.scanLineHeight
        set(value) {
            finderDrawable.scanLineHeight = value
            postInvalidate()
        }

    private val updateRunnbale = Runnable {
        update()
    }

    init {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            background = finderDrawable
        }else{
            setBackgroundDrawable(finderDrawable)
        }
    }

    private fun update(){
        finderDrawable.nextStep()
        if(!isDetached){
            handler.postDelayed(updateRunnbale,QRConstant.FINDER_ANIMATION_DELAY)
        }
    }

    override fun inFrameChange(newFrame: Rect) {
        finderDrawable.inFrameChange(newFrame)
        postInvalidate()
    }

    override fun addPossibleResult(point: ResultPoint) {
        finderDrawable.addPossibleResult(point)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        update()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updateRunnbale)
        isDetached = true
    }

    override fun rotetionChange(rotation: Int) {
        finderDrawable.rotetionChange(rotation)
        postInvalidate()
    }

    fun getRotetion(): Int{
        return finderDrawable.getRotetion()
    }

    override fun start() {
        finderDrawable.start()
    }

    override fun stop() {
        finderDrawable.stop()
    }

}