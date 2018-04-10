package liang.lollipop.lqr.view

import android.graphics.Canvas
import android.graphics.Rect
import com.google.zxing.ResultPoint

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 二维码识别窗口的扫描View叠加层接口
 */
interface QRFinder {

    fun inFrameChange(newFrame: Rect)

    fun addPossibleResult(point: ResultPoint)

    fun draw(canvas: Canvas?)

    fun rotetionChange(rotation:Int)

    fun start()

    fun stop()

}