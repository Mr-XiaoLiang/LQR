package liang.lollipop.lqr.view

import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.FloatRange
import com.google.zxing.ResultPoint
import java.util.*

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 二维码扫描框的动画叠加层
 */
open class QRFinderDrawable: Drawable(),QRFinder {

    /**
     * 扫描框的范围
     */
    protected val frame = RectF()

    /**
     * 新的可能点
     */
    protected var possibleResultPoints = LinkedList<ResultPoint>()
    /**
     * 旧的可能点
     * 将每个可能点都显示2帧
     * 同时，新的可能点显示为较大的点，旧的可能点显示为小的点
     */
    protected var lastPossibleResultPoints = LinkedList<ResultPoint>()

    /**
     * 蒙板的颜色，默认颜色取值为50%透明度的黑色(#80000000)
     */
    var maskColor = 0x80000000.toInt()

    /**
     * 边框的颜色，取色为绿色
     */
    var borderColor = Color.GREEN

    /**
     * 扫描线的颜色
     */
    var scanLineColor = Color.GREEN

    /**
     * 边框的宽度
     */
    var borderWidth = 2F

    /**
     * 可能点的颜色，用于绘制扫描框内的小点
     */
    var pointColor = Color.GREEN

    /**
     * 小点的尺寸
     */
    var pointRadius = 6F

    /**
     * 小号的识别点半径
     */
    var miniPointRadius = pointRadius * 0.5F

    /**
     * 扫描框高度
     */
    var scanLineHeight = 5

    /**
     * 数据的旋转角度
     */
    private var rotation:Int = 0

    private var isStart = false

    @FloatRange(from = 0.0, to = 1.0)
    var scanProgress = 0F

    private var linearGradient = LinearGradient(
            frame.left, frame.top, frame.right, frame.top,
            intArrayOf(0x00FFFFFF, scanLineColor,0x00FFFFFF),
            null, Shader.TileMode.CLAMP)

    protected val paint = Paint()

    /**
     * 扫描方向
     */
    private var scanDirection = true

    override fun draw(canvas: Canvas?) {
        //如果画布为空，放弃绘制
        canvas?:return
        //绘制蒙板
        drawMask(canvas)
        //绘制扫描线
        drawScanLine(canvas)
        //绘制边框
        drawBorder(canvas)
        //绘制小点
        drawPoints(canvas)
    }

    private fun drawMask(canvas: Canvas){
        //绘制蒙板部分的颜色
        paint.style = Paint.Style.FILL
        paint.color = maskColor
//        //保存画布状态
//        canvas.save()
//        //剪裁窗口部分
//        canvas.clipRect(frame)
//        //绘制蒙板部分的颜色
//        paint.style = Paint.Style.FILL
//        paint.color = maskColor
//        canvas.drawRect(bounds,paint)
//        //恢复画板状态
//        canvas.restore()

        canvas.drawRect(0f, 0f, bounds.right.toFloat(), frame.top, paint)//上
        canvas.drawRect(0f, frame.top, frame.left, frame.bottom, paint)//左
        canvas.drawRect(frame.right, frame.top, bounds.right.toFloat(), frame.bottom, paint)//右
        canvas.drawRect(0f, frame.bottom, bounds.right.toFloat(), bounds.bottom.toFloat(), paint)//下
    }

    private fun drawScanLine(canvas: Canvas){
        //保存画布状态
        canvas.save()
        //剪裁窗口部分
        canvas.clipRect(frame)

        val top = scanProgress * frame.height() + frame.top
        val left = frame.left
        val bottom = top + scanLineHeight
        val right = frame.right
        paint.color = borderColor
        paint.shader = linearGradient
        paint.style = Paint.Style.FILL
        canvas.drawRect(left, top, right, bottom, paint)//画扫描线
        paint.shader = null

        canvas.restore()
    }

    private fun drawBorder(canvas: Canvas){
        paint.strokeWidth = borderWidth
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        canvas.drawRect(frame,paint)
    }

    private fun drawPoints(canvas: Canvas){
        paint.color = pointColor
        paint.style = Paint.Style.FILL

        //保存画布状态
        canvas.save()
        //旋转窗口
        canvas.rotate(-1F * rotation,canvas.width * 0.5f,canvas.height * 0.5f)

        val possibleArray: Array<ResultPoint?> = Array(possibleResultPoints.size,{ it -> possibleResultPoints[it] })
        //绘制新的小点
        for(point in possibleArray){
            point?:continue
            canvas.drawCircle(frame.left + point.x,frame.top + point.y,pointRadius,paint)
        }
        //绘制旧的小点
        for(point in lastPossibleResultPoints){
            canvas.drawCircle(frame.left + point.x,frame.top + point.y,miniPointRadius,paint)
        }

        canvas.restore()

        lastPossibleResultPoints.clear()
        lastPossibleResultPoints.addAll(possibleArray.filterNotNull())
        possibleResultPoints.clear()

    }

    override fun rotetionChange(rotation: Int) {
        this.rotation = rotation
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun inFrameChange(newFrame: Rect) {
        frame.set(newFrame)
        linearGradient = LinearGradient(
                frame.left, frame.top, frame.right, frame.top,
                intArrayOf(Color.TRANSPARENT, scanLineColor, Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP)
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        invalidateSelf()
    }

    fun nextStep(){
        if(scanDirection){
            scanProgress += 0.01F
        }else{
            scanProgress -= 0.01F
        }
        if(scanProgress > 1){
            scanDirection = false
        }
        if(scanProgress < 0){
            scanDirection = true
        }
        invalidateSelf()
    }

    override fun addPossibleResult(point: ResultPoint) {
        if(isStart){
            possibleResultPoints.add(point)
        }
    }

    fun getRotetion(): Int{
        return rotation
    }

    override fun start() {
        isStart = true
    }

    override fun stop() {
        possibleResultPoints.clear()
        lastPossibleResultPoints.clear()
        isStart = false
    }

}