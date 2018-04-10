package liang.lollipop.lqr.decode

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 解码工具类
 */
class DecodeUtil {

    var reverseHorizontal = false

    private val multiFormatReader: MultiFormatReader = MultiFormatReader()

    fun setDecodeHints(hints: Map<DecodeHintType, Any>){
        multiFormatReader.setHints(hints)
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    fun decodeYUV(data: ByteArray,width: Int, height: Int,decodeFrame: Rect): DecodeResult? {
        val source = if(reverseHorizontal){
            val rotatedData = ByteArray(data.size)
            for (y in 0 until height) {
                for (x in 0 until width)
                    rotatedData[x * height + height - y - 1] = data[x + y * width]
            }
            buildLuminanceSource(rotatedData, width, height, decodeFrame)
        }else{
            buildLuminanceSource(data, width, height, decodeFrame)
        }
        return decodeQR(source)
    }

    fun decodeBitmap(bitmap: Bitmap?): DecodeResult? {
        bitmap?:return null
        val pixels = IntArray(bitmap.height * bitmap.width)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        return decodeQR(source)
    }


    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    private fun buildLuminanceSource(data: ByteArray, width: Int, height: Int,decodeFrame:Rect): PlanarYUVLuminanceSource {
        Log.d("buildLuminanceSource","width:$width, height:$height,decodeFrame.left:${decodeFrame.left}, decodeFrame.top:${decodeFrame.top},decodeFrame.width():${decodeFrame.width()}, decodeFrame.height():${ decodeFrame.height()}")
        return PlanarYUVLuminanceSource(data, width, height, decodeFrame.left, decodeFrame.top,
                decodeFrame.width(), decodeFrame.height(),false)
    }

    /**
     * 对图像进行解码
     */
    private fun decodeQR(source: LuminanceSource): DecodeResult? {
        var rawResult: Result? = null
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap)
        } catch (re: ReaderException) {
            // continue
        } finally {
            multiFormatReader.reset()
        }
        if(rawResult != null){
            return DecodeResult(source,rawResult)
        }
        return null
    }

    class DecodeResult(val source: LuminanceSource,val result: Result){
        override fun toString(): String {
            return "DecodeResult [source:$source,result:${result.text}]"
        }
    }

}