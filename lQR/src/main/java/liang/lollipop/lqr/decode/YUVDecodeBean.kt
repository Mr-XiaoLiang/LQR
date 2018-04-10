package liang.lollipop.lqr.decode

import android.graphics.Rect

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 相机预览画面传输至解码队列的Bean
 */
class YUVDecodeBean(val data: ByteArray,val width: Int,val height: Int,val decodeFrame: Rect){

    var reverseHorizontal = false

    override fun toString(): String {
        return "YUVDecodeBean [data:${data.size},width:$width,height:$height,decodeFrame:$decodeFrame]"
    }
}