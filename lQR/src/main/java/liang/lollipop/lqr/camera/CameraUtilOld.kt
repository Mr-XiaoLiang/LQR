package liang.lollipop.lqr.camera

import android.app.Activity
import android.graphics.Point
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import liang.lollipop.lqr.decode.YUVDecodeBean
import liang.lollipop.lqr.util.LQRTask

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 老式的相机接口API管理工具
 */
class CameraUtilOld private constructor(previewType: PreviewType,
                    textureView: TextureView?,
                    surfaceView: SurfaceView?,
                    callback: CameraCallback) : CameraUtil(previewType,textureView,surfaceView,callback){

    constructor(textureView: TextureView,cameraCallback: CameraCallback):
            this(PreviewType.TEXTURE_VIEW,textureView,null,cameraCallback)

    constructor(surfaceView: SurfaceView,cameraCallback: CameraCallback):
            this(PreviewType.SURFACE_VIEW,null,surfaceView,cameraCallback)

    private var camera:Camera? = null

    private val lastPreviewSize = Point(0,0)

    private val autoFocusCallback = object :Camera.AutoFocusCallback{

        private val AUTOFOCUS_INTERVAL_MS = 1500L

        private var autoFocusHandler: Handler? = null
        private var autoFocusMessage: Int = 0

        fun withHandler(handler: Handler,msg:Int): Camera.AutoFocusCallback{
            this.autoFocusHandler = handler
            this.autoFocusMessage = msg
            return this
        }

        override fun onAutoFocus(success: Boolean, camera: Camera?) {

            if (autoFocusHandler != null) {
                val message = autoFocusHandler?.obtainMessage(autoFocusMessage, success)
                autoFocusHandler?.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS)
                autoFocusHandler = null
            }

        }
    }

    private val previewCallback = object :Camera.PreviewCallback{
        private var previewHandler: Handler? = null
        private var previewMessage: Int = 0

        fun setHandler(previewHandler: Handler, previewMessage: Int) {
            this.previewHandler = previewHandler
            this.previewMessage = previewMessage
        }

        override fun onPreviewFrame(data: ByteArray, camera: Camera) {

            if (previewHandler != null) {

                val size = getCameraPreviewSize()

                val rect = getDecodeFrame()
                val decodeBean = YUVDecodeBean(data,size.width, size.height,rect)
                val message = previewHandler?.obtainMessage(previewMessage, decodeBean)
                message?.sendToTarget()
                previewHandler = null
            }
        }
    }

    override fun changeFlash(isOpen: Boolean) {

        if (state == State.PREVIEW && camera != null ) {
            val mParameters = camera?.parameters
            if (isOpen) {
                mParameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF//打开Camera.Parameters.FLASH_MODE_OFF则为关闭
            } else {
                mParameters?.flashMode = Camera.Parameters.FLASH_MODE_TORCH//打开Camera.Parameters.FLASH_MODE_TORCH则为开启
            }
            camera?.parameters = mParameters
        }

    }

    override fun requestPreviewFrame(handler: Handler, code: Int) {

        if (state == State.PREVIEW && camera != null) {
            previewCallback.setHandler(handler, code)
            camera?.setPreviewCallback(previewCallback)
        }

    }

    override fun requestFocus(handler: Handler, code: Int) {
        if(state == State.PREVIEW){
            try{
                camera?.autoFocus(autoFocusCallback.withHandler(handler,code))
            }catch (e: Exception){
                cameraCallback.onCameraError(e)
            }
        }
    }

    override fun startPreview(context: Activity) {
        lastPreviewSize.set(0,0)

        val numberOfCameras = Camera.getNumberOfCameras()
        val cameraIds = Array(numberOfCameras,{ it -> "$it" })

        val cameraId = cameraCallback.selectCamera(cameraIds)

        val cameraIndex = cameraIds.indexOf(cameraId)

        cameraCallback.onCameraOpening()

        state = State.PAUSE

        LQRTask.addTask(object :LQRTask.CallBack<Camera,Int>{
            override fun success(result: Camera) {
                camera = result
                state = State.READY
                onPreview()
            }

            override fun error(e: Exception?) {
                state = State.DONE
                camera = null
                cameraCallback.onCameraError(e)
            }

            override fun processing(args: Int?): Camera {
                return if(args == null || args < 0){
                    Camera.open()
                }else{
                    Camera.open(args)
                }
            }
        },cameraIndex)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        if(state == State.PREVIEW || state == State.READY){
            if (holder?.surface == null){
                return
            }
            if(camera == null){
                return
            }
            try {
                camera?.stopPreview()
            } catch (e: Exception) { }

            onPreview()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        isSurfaceReady = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        isSurfaceReady = true
        if(camera!=null && state == State.READY){
            onPreview()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        if(state == State.PREVIEW || state == State.READY){
            if (surface == null){
                return
            }
            if(camera == null){
                return
            }
            try {
                camera?.stopPreview()
            } catch (e: Exception) { }

            onPreview()
        }
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        //主动刷新的方法
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        isSurfaceReady = false
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        isSurfaceReady = true
        if(camera!=null && state == State.READY){
            onPreview()
        }
    }

    private fun onPreview(){
        if(camera == null){
            cameraCallback.onCameraError(RuntimeException("camera is Null"))
            return
        }

        if(state != State.READY){
            cameraCallback.onCameraError(RuntimeException("camera is not open"))
            return
        }

        state = State.OPENING

        try {

            when(previewType){

                PreviewType.SURFACE_VIEW -> if(isSurfaceReady && surfaceView != null){
                    camera?.setPreviewDisplay(surfaceView.holder)
                }

                PreviewType.TEXTURE_VIEW -> if(isSurfaceReady && textureView != null){
                    camera?.setPreviewTexture(textureView.surfaceTexture)
                }

            }
            setRotation()

            camera?.startPreview()

            state = State.PREVIEW
            cameraCallback.onCameraOpen()

        }catch (exception:Exception){
            cameraCallback.onCameraError(exception)
        }

    }

    private fun setRotation(){
        orientation = getRotation()
        camera?.setDisplayOrientation(orientation)
        val parameter = camera?.parameters
        parameter?.setRotation(orientation)
        camera?.parameters = parameter
    }

    override fun stopPreview() {
        state = State.DONE
        synchronized(Camera::class.java){
            if(camera != null){
                camera?.setPreviewCallback(null)
                camera?.stopPreview()
                camera?.release()
                camera = null
            }
        }
    }

    override fun getRotation(): Int{
        return sensorToDeviceRotation(windowManager.defaultDisplay.rotation)
    }

    private fun sensorToDeviceRotation(deviceOrientation: Int): Int {
        val sensorOrientation = 90 * if(deviceOrientation % 2 == 0){ 1 }else{ -1 }

        val orientationRotation = ORIENTATIONS.get(deviceOrientation)
        return (sensorOrientation + orientationRotation + 360) % 360
    }

    override fun getDecodeFrame(): Rect {
        val rect = decodeFrameRect

        if(camera == null){
            return rect
        }

        val size = getCameraPreviewSize()

        if(size.width == lastPreviewSize.x && size.height == lastPreviewSize.y){
            return rect
        }

        val previewFrame = getPreviewFrame()
        val previewSize = getPreviewSize()

        val weight = if(orientation % 180 == 0){
            Math.min(1.0f * size.width / previewSize.x,1.0f * size.height / previewSize.y)
        }else{
            Math.min(1.0f * size.height / previewSize.x,1.0f * size.width / previewSize.y)
        }

        val width = Math.min(weight * previewFrame.width(),weight * previewFrame.height())

        val left = (size.width - width)/2 + frameOffset.y * weight
        val top = (size.height - width)/2 + frameOffset.x * weight

        rect.set(left.toInt(),top.toInt(),(left+width).toInt(),(top+width).toInt())

        lastPreviewSize.set(size.width,size.height)

        return rect
    }

    private fun getCameraPreviewSize(): Camera.Size {
        return camera!!.parameters.previewSize
    }

}