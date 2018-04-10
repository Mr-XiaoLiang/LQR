package liang.lollipop.lqr.decode

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.google.zxing.PlanarYUVLuminanceSource
import java.io.ByteArrayOutputStream


/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 解码的队列
 */
class DecodeHandler(private val decodeUtil: DecodeUtil,
                    private val resultHandler: Handler,
                    private val resultCode: DecodeThread.ResultCode): Handler() {

    private var running = true

    companion object {

        private const val TAG = "DecodeHandler"

        const val DECODE = 456

        const val DECODE_BITMAP = 457

        const val QUIT = 400

    }

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
        if(!running){
            return
        }
        when(msg?.what){

            DECODE -> {
                val dataBean = msg.obj
                if(dataBean is YUVDecodeBean){
//                    try{
                        decodeUtil.reverseHorizontal = dataBean.reverseHorizontal
                        val resultBean = decodeUtil.decodeYUV(dataBean.data,dataBean.width,dataBean.height,dataBean.decodeFrame)
                        val resultMessage = if(resultBean == null){
                            resultHandler.obtainMessage(resultCode.failedCode)
                        }else{
                            resultHandler.obtainMessage(resultCode.succeededCode,resultBean.result).apply {
                                data = bundleYUVThumbnail(resultBean.source as PlanarYUVLuminanceSource,Bundle())
                            }
                        }
                        resultHandler.sendMessage(resultMessage)
//                    }catch (e:Exception){
////                        e.printStackTrace()
//                        resultHandler.sendEmptyMessage(resultCode.failedCode)
//                    }
//                    Log.d(TAG,"YUVDecodeBean:$dataBean,resultBean:${resultBean?:"null"}")
                }else{
                    resultHandler.sendEmptyMessage(resultCode.failedCode)
//                    Log.d(TAG,"YUVDecodeBean:$dataBean")
                }
            }

            DECODE_BITMAP -> {
                val dataBean = msg.obj
                if(dataBean is BitmapDecodeBean){
//                    try {
                        val resultBean = decodeUtil.decodeBitmap(dataBean.data)
                        val resultMessage = if(resultBean == null){
                            resultHandler.obtainMessage(resultCode.failedCode)
                        }else{
                            resultHandler.obtainMessage(resultCode.succeededCode,resultBean.result)
                        }
                        resultHandler.sendMessage(resultMessage)
//                    }catch (e: Exception){
//                        e.printStackTrace()
//                        resultHandler.sendEmptyMessage(resultCode.failedCode)
//                    }

                }else{
                    resultHandler.sendEmptyMessage(resultCode.failedCode)
                }
            }

            QUIT -> {
                Looper.myLooper().quit()
            }

        }
    }

    private fun bundleYUVThumbnail(source: PlanarYUVLuminanceSource, bundle: Bundle): Bundle {
        val pixels = source.renderThumbnail()
        val width = source.thumbnailWidth
        val height = source.thumbnailHeight
        val bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray())
        bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, width.toFloat() / source.width)

        return bundle
    }

}