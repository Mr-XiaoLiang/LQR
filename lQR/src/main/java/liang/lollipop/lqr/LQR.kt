package liang.lollipop.lqr

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import liang.lollipop.lqr.callback.BitmapCallback
import liang.lollipop.lqr.callback.BitmapDrawableCallback
import liang.lollipop.lqr.callback.ImageViewCallback
import liang.lollipop.lqr.camera.*
import liang.lollipop.lqr.decode.CaptureCallback
import liang.lollipop.lqr.decode.CaptureHandler
import liang.lollipop.lqr.encode.LBitMatrix
import liang.lollipop.lqr.encode.LQRCodeWriter
import liang.lollipop.lqr.util.LQRTask
import liang.lollipop.lqr.view.QRFinder

/**
 * Created by lollipop on 2018/3/13.
 * @author Lollipop
 * 二维码工具类调用的入口
 */
class LQR private constructor(private val content: String) {

    private val hints = HashMap<EncodeHintType,Any>()
    private var width = -1
    private var height = -1
    private var format:BarcodeFormat = BarcodeFormat.QR_CODE
    private var miniQR = false

    var darkColor = Color.BLACK
    var lightColor = Color.WHITE

    companion object {

        fun with(content: String): LQR{
            return LQR(content)
        }

        const val MIN_WIDTH = 21

        private const val TAG = "LQR"

        fun capture(version: CameraVersion, textureView: TextureView, cameraCallback: CameraCallback,
                    decodeFormats: Collection<BarcodeFormat>?, baseHints: Map<DecodeHintType,Any>?,
                    characterSet:String?, finder: QRFinder,
                    captureCallback: CaptureCallback): CaptureHandler{
            val captureCameraCallback = CaptureHandler.CaptureCameraCallback(cameraCallback)
            val cameraUtil = getCameraUtil(version,textureView, captureCameraCallback)
            return CaptureHandler(decodeFormats, baseHints, characterSet, finder, cameraUtil, captureCallback,captureCameraCallback)

        }

        fun capture(version: CameraVersion, textureView: TextureView,
                    cameraCallback: CameraCallback, finder: QRFinder,
                    captureCallback: CaptureCallback): CaptureHandler{
            return capture(version, textureView, cameraCallback,null,null,null, finder, captureCallback)
        }

        fun capture(textureView: TextureView,
                    cameraCallback: CameraCallback, finder: QRFinder,
                    captureCallback: CaptureCallback): CaptureHandler{
            return capture(CameraVersion.AUTO, textureView, cameraCallback,finder, captureCallback)

        }

        @SuppressLint("NewApi")
        fun capture(version: CameraVersion, surfaceView: SurfaceView, cameraCallback: CameraCallback,
                    decodeFormats: Collection<BarcodeFormat>?, baseHints: Map<DecodeHintType,Any>?,
                    characterSet:String?, finder: QRFinder,
                    captureCallback: CaptureCallback): CaptureHandler{
            val captureCameraCallback = CaptureHandler.CaptureCameraCallback(cameraCallback)
            val cameraUtil = getCameraUtil(version,surfaceView, captureCameraCallback)
            return CaptureHandler(decodeFormats, baseHints, characterSet, finder, cameraUtil, captureCallback,captureCameraCallback)
        }

        fun capture(version: CameraVersion, surfaceView: SurfaceView,
                    cameraCallback: CameraCallback,finder: QRFinder,
                    captureCallback: CaptureCallback): CaptureHandler{
            return capture(version, surfaceView, cameraCallback,null,null,null, finder, captureCallback)
        }

        fun capture(surfaceView: SurfaceView,
                    cameraCallback: CameraCallback,finder: QRFinder,
                    captureCallback: CaptureCallback): CaptureHandler{
            return capture(CameraVersion.AUTO, surfaceView, cameraCallback, finder, captureCallback)
        }

        @SuppressLint("NewApi")
        private fun getCameraUtil(version: CameraVersion, textureView: TextureView, cameraCallback: CameraCallback): CameraUtil{
            when(version){

                CameraVersion.AUTO -> {
                    return if(Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT){
                        getCameraUtil(CameraVersion.LOLLIPOP,textureView, cameraCallback)
                    }else{
                        getCameraUtil(CameraVersion.OLD,textureView, cameraCallback)
                    }
                }

                CameraVersion.OLD -> {
                    return CameraUtilOld(textureView, cameraCallback)
                }

                CameraVersion.LOLLIPOP -> {
                    return CameraUtilNew(textureView,cameraCallback)
                }

            }
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun getCameraUtil(version: CameraVersion, surfaceView: SurfaceView, cameraCallback: CameraCallback): CameraUtil{
            when(version){

                CameraVersion.AUTO -> {
                    return if(Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT){
                        getCameraUtil(CameraVersion.LOLLIPOP,surfaceView, cameraCallback)
                    }else{
                        getCameraUtil(CameraVersion.OLD,surfaceView, cameraCallback)
                    }
                }

                CameraVersion.OLD -> {
                    return CameraUtilOld(surfaceView, cameraCallback)
                }

                CameraVersion.LOLLIPOP -> {
                    return CameraUtilNew(surfaceView,cameraCallback)
                }

            }
        }

    }

    init {

        hints[EncodeHintType.CHARACTER_SET] = "utf-8"
        // 设置QR二维码的纠错级别（H为最高级别）具体级别信息
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H

    }

    fun hint(type: EncodeHintType,value: Any): LQR{
        hints[type] = value
        return this
    }

    fun size(size: Int): LQR{
        width = size
        height = size
        return this
    }

    fun barcodeFormat(format: BarcodeFormat): LQR{
        this.format = format
        return this
    }

    fun miniQR(): LQR{
        return miniQR(true)
    }

    fun miniQR(type: Boolean): LQR{
        this.miniQR = type
        return this
    }

    fun darkColor(color: Int): LQR{
        darkColor = color
        return this
    }

    fun lightColor(color: Int): LQR{
        lightColor = color
        return this
    }

    fun encode(): LBitMatrix {
        return if(format == BarcodeFormat.QR_CODE){
            LQRCodeWriter(if (miniQR) {
                LQRCodeWriter.WriterType.MINI
            } else {
                LQRCodeWriter.WriterType.DEFAULT
            })
                    .encode2(content,format,width,height,hints)
        }else{
            LBitMatrix.copyOf(MultiFormatWriter().encode(content,format,width,height,hints))
        }
    }

    fun into(imageView:ImageView): ImageView{
        Log.d(TAG,"into ImageView start:${System.currentTimeMillis()}")
        val drawable: BitmapDrawable = if(imageView.drawable is BitmapDrawable){
            val imageDrawable = imageView.drawable as BitmapDrawable
            val bitmap = imageDrawable.bitmap
            //如果尺寸不一致，那么重新创建一个
            if(bitmap.width != imageView.width || bitmap.height != imageView.height){
                BitmapDrawable(imageView.resources,
                        Bitmap.createBitmap(imageView.width,
                                imageView.height,
                                Bitmap.Config.ARGB_8888))
            }else{
                imageDrawable
            }

        }else{
            BitmapDrawable(imageView.resources,
                    Bitmap.createBitmap(imageView.width,
                            imageView.height,
                            Bitmap.Config.ARGB_8888))
        }
        val canvas = Canvas(drawable.bitmap)
        imageView.draw(canvas)
        imageView.setImageDrawable(into(drawable))
        Log.d(TAG,"into ImageView end:${System.currentTimeMillis()}")
        return imageView
    }

    fun into(drawable: BitmapDrawable): BitmapDrawable{
        Log.d(TAG,"into BitmapDrawable start:${System.currentTimeMillis()}")
        val bitmap = create(drawable.bitmap)
        if(bitmap != drawable.bitmap){
            return BitmapDrawable(null,bitmap)
        }
        Log.d(TAG,"into BitmapDrawable end:${System.currentTimeMillis()}")
        return drawable
    }

    fun create(bm: Bitmap? = null): Bitmap{
        Log.d(TAG,"create Bitmap start:${System.currentTimeMillis()}")

        val bitMatrix: LBitMatrix

        val bitmap = when{
            bm == null -> {
                bitMatrix = encode()
                width = Math.max(width,bitMatrix.width)
                height = Math.max(height,bitMatrix.height)
                Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
            }

            !bm.isMutable -> {
                height = Math.min(bm.height,bm.width)
                width = height
                bitMatrix = encode()
                Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
            }

            else -> {
                height = Math.min(bm.height,bm.width)
                width = height
                bitMatrix = encode()
                bm
            }

        }
        if(width < MIN_WIDTH || height < MIN_WIDTH){
            throw RuntimeException("Width and height must be greater than 21")
        }
        drawBitmap2(bitMatrix,bitmap,bm)
        Log.d(TAG,"create Bitmap end:${System.currentTimeMillis()}")
        return bitmap
    }

    private fun drawBitmap(bitMatrix: LBitMatrix, bitmap: Bitmap, src: Bitmap? = null): Bitmap{
        Log.d(TAG,"draw Bitmap start:${System.currentTimeMillis()}")
        for(x in 0 until bitMatrix.width){
            for(y in 0 until bitMatrix.height){
                if(bitMatrix.isNotNull(x, y)){
                    bitmap.setPixel(x,y,
                            if(bitMatrix.isBlack(x, y)){
                                darkColor
                            }else{
                                lightColor
                            }
                    )
                }else if(src != null){
                    bitmap.setPixel(x,y,src.getPixel(x, y))
                }
            }
        }
        Log.d(TAG,"draw Bitmap end:${System.currentTimeMillis()}")
        return bitmap
    }

    private fun drawBitmap2(bitMatrix: LBitMatrix, bitmap: Bitmap, src: Bitmap? = null): Bitmap{
        Log.d(TAG,"draw Bitmap start:${System.currentTimeMillis()}")
        //创建一个空的像素数组
        val pixelArray = IntArray(bitmap.width * bitmap.width, { Color.WHITE })
        //如果有原始图片，那么就将它复制到现有像素数组
        src?.getPixels(pixelArray,0,bitmap.width,0,0,bitmap.width,bitmap.height)
        //将二维码赋值到现有像素数组
        getPixelArray(bitMatrix,pixelArray)
        //将像素数组赋值到图片中
        bitmap.setPixels(pixelArray,0,bitmap.width,0,0,bitmap.width,bitmap.height)
        Log.d(TAG,"draw Bitmap end:${System.currentTimeMillis()}")
        return bitmap
    }

    fun getPixels(): IntArray{
        return getPixelArray(encode())
    }

    private fun getPixelArray(bitMatrix: LBitMatrix): IntArray{
        return getPixelArray(bitMatrix,IntArray(bitMatrix.width * bitMatrix.width, { Color.WHITE }) )
    }

    private fun getPixelArray(bitMatrix: LBitMatrix, pixelArray: IntArray): IntArray{
        for(x in 0 until bitMatrix.width){
            for(y in 0 until bitMatrix.height){
                if(bitMatrix.isNotNull(x, y)){
                    pixelArray[y * width + x] = if(bitMatrix.isBlack(x, y)){
                        darkColor
                    }else{
                        lightColor
                    }
                }
            }
        }
        return pixelArray
    }

    fun ansyTo(view: ImageView, callback: ImageViewCallback){

        LQRTask.addTask(object : LQRTask.CallBack<BitmapDrawable, ImageView>{

            override fun success(result: BitmapDrawable) {
                view.setImageDrawable(result)
                callback.onSuccess(view)
            }

            override fun error(e: Exception?) {
                callback.onError(e)
            }

            override fun processing(args: ImageView?): BitmapDrawable {
                Log.d(TAG,"ansyTo ImageView start:${System.currentTimeMillis()}")

                val imageView = args!!
                val drawable: BitmapDrawable = if(imageView.drawable is BitmapDrawable){
                    val imageDrawable = imageView.drawable as BitmapDrawable
                    val bitmap = imageDrawable.bitmap
                    //如果尺寸不一致，那么重新创建一个
                    if(bitmap.width != imageView.width || bitmap.height != imageView.height){
                        BitmapDrawable(imageView.resources,
                                Bitmap.createBitmap(imageView.width,
                                        imageView.height,
                                        Bitmap.Config.ARGB_8888))
                    }else{
                        imageDrawable
                    }

                }else{
                    BitmapDrawable(imageView.resources,
                            Bitmap.createBitmap(imageView.width,
                                    imageView.height,
                                    Bitmap.Config.ARGB_8888))
                }
                if(drawable != imageView.drawable){
                    val canvas = Canvas(drawable.bitmap)
                    imageView.draw(canvas)
                }
                val result = into(drawable)
                Log.d(TAG,"ansyTo ImageView end:${System.currentTimeMillis()}")

                return result

            }

        },view)

    }

    fun ansyTo(drawable: BitmapDrawable, bitmapDrawableCallback: BitmapDrawableCallback){

        LQRTask.addTask(object : LQRTask.CallBack<BitmapDrawable, BitmapDrawable>{
            override fun success(result: BitmapDrawable) {
                bitmapDrawableCallback.onSuccess(result)
            }

            override fun error(e: Exception?) {
                bitmapDrawableCallback.onError(e)
            }

            override fun processing(args: BitmapDrawable?): BitmapDrawable {
                return into(args!!)
            }
        },drawable)

    }

    fun ansyTo(bitmap: Bitmap?,bitmapCallback: BitmapCallback){
        LQRTask.addTask(object : LQRTask.CallBack<Bitmap,Bitmap>{
            override fun success(result: Bitmap) {
                bitmapCallback.onSuccess(result)
            }

            override fun error(e: Exception?) {
                bitmapCallback.onError(e)
            }

            override fun processing(args: Bitmap?): Bitmap {
                return create(args)
            }

        },bitmap)
    }

    fun ansyTo(bitmapCallback: BitmapCallback){
        ansyTo(null,bitmapCallback)
    }

}