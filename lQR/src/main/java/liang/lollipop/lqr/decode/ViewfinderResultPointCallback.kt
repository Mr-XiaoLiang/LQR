package liang.lollipop.lqr.decode

import com.google.zxing.ResultPoint
import com.google.zxing.ResultPointCallback
import liang.lollipop.lqr.view.QRFinder

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 识别点回调函数
 */
class ViewfinderResultPointCallback(private val finder: QRFinder): ResultPointCallback {

    var scale = 0F

    override fun foundPossibleResultPoint(point: ResultPoint?) {
        point?:return
        finder.addPossibleResult(ResultPoint(point.x * scale,point.y * scale))
    }

}