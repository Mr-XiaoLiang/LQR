package liang.lollipop.lqr.decode

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.Result
import liang.lollipop.lqr.camera.CameraCallback
import liang.lollipop.lqr.camera.CameraUtil
import liang.lollipop.lqr.view.QRFinder


/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 捕获图像用的Handler
 */
class CaptureHandler(decodeFormats: Collection<BarcodeFormat>?, baseHints: Map<DecodeHintType,Any>?,
                     characterSet:String?,
                     private val finder: QRFinder,private val cameraUtil: CameraUtil,
                     private val captureCallback: CaptureCallback,captureCameraCallback: CaptureCameraCallback)
    : Handler(),CameraCallback,CameraUtil.OnDecodeFrameChangeCallback {

    companion object {

        private const val DECODE_SUCCEEDED = 345
        private const val DECODE_FAILED = 346

        private const val RESTART_PREVIEW = 347

        private const val AUTO_FOCUS = 348

    }

    private val resultPointCallback = ViewfinderResultPointCallback(finder)

    private val decodeThread = DecodeThread(
            this, DecodeThread.ResultCode(DECODE_SUCCEEDED,DECODE_FAILED),
            decodeFormats,baseHints,characterSet,resultPointCallback)

    private var state: State = State.SUCCESS

    init {

        decodeThread.start()
        captureCameraCallback.captureCallback = this
        cameraUtil.onDecodeFrameChangeCallback = this

    }

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
        msg?:return
        if(state == State.DONE){
            return
        }
        when(msg.what){

            DECODE_SUCCEEDED -> {

                state = State.SUCCESS
                val bundle = msg.data
                var barcode: Bitmap? = null
                var scaleFactor = 1.0f
                if(bundle != null){
                    val compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP)
                    if (compressedBitmap != null) {
                        val temp = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.size, null)
                        // Mutable copy:
                        barcode = temp.copy(Bitmap.Config.ARGB_8888, true)
                        temp.recycle()
                    }
                    scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR)
                }
                captureCallback.onSuccess(msg.obj as Result,barcode,scaleFactor)

            }

            DECODE_FAILED -> {

                state = State.PREVIEW
                cameraUtil.requestPreviewFrame(decodeThread.getHandler(), DecodeThread.DECODE)

            }

            RESTART_PREVIEW -> {
                restartPreviewAndDecode()
            }

            AUTO_FOCUS -> {

                cameraUtil.requestFocus(this, AUTO_FOCUS)

            }

        }
    }

    override fun onCameraOpen() {
        val rotation = cameraUtil.getRotation()
        finder.rotetionChange(rotation % 180)
        onFrameChange()
        sendEmptyMessage(AUTO_FOCUS)
        restartPreviewAndDecode()
    }

    override fun onCameraError(exception: Exception?) {
    }

    override fun onCameraOpening() {
    }

    override fun selectCamera(cameraIds: Array<String>): String {
        if(cameraIds.isEmpty()){
            return "0"
        }
        return cameraIds[0]
    }

    override fun onDecodeFrameChange(frame: Rect) {
        val proviewFrame = cameraUtil.getPreviewFrame()
        resultPointCallback.scale = 1.0F * proviewFrame.width() / frame.width()
        Log.d("onFrameChange","decodeFrame:$frame,proviewFrame:$proviewFrame")
        finder.inFrameChange(proviewFrame)
    }

    private fun restartPreviewAndDecode() {
        if (state == State.SUCCESS || state == State.RESUME) {
            state = State.PREVIEW
            cameraUtil.requestPreviewFrame(decodeThread.getHandler(), DecodeThread.DECODE)
        }
    }

    private fun quitSynchronously() {
        state = State.DONE
        cameraUtil.stopPreview()
        val quit = Message.obtain(decodeThread.getHandler(), DecodeThread.QUIT)
        quit.sendToTarget()
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L)
        } catch (e: InterruptedException) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(DECODE_SUCCEEDED)
        removeMessages(DECODE_FAILED)
    }

    fun onDestroy(){
        quitSynchronously()
    }

    fun onStop(){

        finder.stop()

        if(state == State.DONE){
            return
        }
        state = State.PAUSE
        cameraUtil.stopPreview()
        removeMessages(DECODE_SUCCEEDED)
        removeMessages(DECODE_FAILED)
        removeMessages(AUTO_FOCUS)
    }

    fun onStart(activity: Activity){
        finder.start()
        if(state == State.DONE || state == State.PREVIEW){
            return
        }
        state = State.RESUME
        cameraUtil.windowManager = activity.windowManager
        cameraUtil.startPreview(activity)
    }

    fun onResume(){
        cameraUtil.onResume()
    }

    fun onPause(){
        cameraUtil.onPause()
    }

    fun restart(){
        restartPreviewAndDecode()
    }

    private fun onFrameChange(){
        onDecodeFrameChange(cameraUtil.getDecodeFrame())
    }

    fun requestFocus(){
        sendEmptyMessage(AUTO_FOCUS)
    }

    fun changeFlash(isOpen: Boolean){
        if(state == State.PREVIEW){
            cameraUtil.changeFlash(isOpen)
        }
    }

    private enum class State {
        PREVIEW,
        SUCCESS,
        PAUSE,
        RESUME,
        DONE
    }

    class CaptureCameraCallback(private val outputCallback:CameraCallback): CameraCallback{

        var captureCallback:CameraCallback? = null

        override fun onCameraOpen() {
            outputCallback.onCameraOpen()
            captureCallback?.onCameraOpen()
        }

        override fun onCameraError(exception: Exception?) {
            outputCallback.onCameraError(exception)
            captureCallback?.onCameraError(exception)
        }

        override fun onCameraOpening() {
            outputCallback.onCameraOpening()
            captureCallback?.onCameraOpening()
        }

        override fun selectCamera(cameraIds: Array<String>): String {
            captureCallback?.selectCamera(cameraIds)
            return outputCallback.selectCamera(cameraIds)
        }
    }

}