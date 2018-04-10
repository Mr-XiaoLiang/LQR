package liang.lollipop.lqr.decode

import android.graphics.Bitmap
import com.google.zxing.Result

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 捕获的回调函数
 */
interface CaptureCallback {

    fun onSuccess(result: Result, barcode: Bitmap?, scaleFactor: Float)

}