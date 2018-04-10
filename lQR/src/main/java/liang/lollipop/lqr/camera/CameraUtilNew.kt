package liang.lollipop.lqr.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.util.Size
import android.view.*
import liang.lollipop.lqr.decode.YUVDecodeBean
import liang.lollipop.lqr.util.HandlerThread
import liang.lollipop.lqr.util.QRConstant
import java.util.*


/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 新版的相机API接口的工具类
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class CameraUtilNew private constructor(previewType: PreviewType,
                                       textureView: TextureView?,
                                       surfaceView: SurfaceView?,
                                       callback: CameraCallback)
    : CameraUtil(previewType,textureView,surfaceView,callback){

    constructor(textureView: TextureView,cameraCallback: CameraCallback):
            this(PreviewType.TEXTURE_VIEW,textureView,null,cameraCallback)

    constructor(surfaceView: SurfaceView,cameraCallback: CameraCallback):
            this(PreviewType.SURFACE_VIEW,null,surfaceView,cameraCallback)

    //摄像头管理器
    private var cameraManager: CameraManager? = null

    private var cameraId = ""

    private var outputImageReader: ImageReader? = null

    //拍照用的异步线程Handler
    private val captureHandler = HandlerThread("Camera2Capture").apply { start() }.getHandler()
    //相机的主要线程
    private val mainHandler = Handler(Looper.getMainLooper())//HandlerThread("Camera2Main").apply { start() }.getHandler()

    private var cameraDevice: CameraDevice? = null

    //拍照的回调函数
    private val imageReaderCallback = ReadImageCallback()

    private var captureStateCallback: CaptureStateCallback? = null

    private val lastPreviewSize = Point(0,0)

    companion object {

        fun getCodeError(code: Int): String {
            return when (code) {
                CameraDevice.StateCallback.ERROR_CAMERA_IN_USE -> "相机已被使用，请先关闭其他相机应用"
                CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE -> "相机无法打开，全部相机已被占用"
                CameraDevice.StateCallback.ERROR_CAMERA_DISABLED -> "相机无法打开，因为设备政策限制"
                CameraDevice.StateCallback.ERROR_CAMERA_DEVICE -> "相机打开失败，相机驱动遇到致命错误"
                CameraDevice.StateCallback.ERROR_CAMERA_SERVICE -> "相机打开失败，相机服务遇到致命错误"
                else -> "未记录的异常类型，Code:$code"
            }
        }

    }

    override fun requestPreviewFrame(handler: Handler, code: Int) {
        if(state == State.PREVIEW){
            imageReaderCallback.ready(handler, code)
        }
    }

    override fun requestFocus(handler: Handler, code: Int) {
        if(state == State.PREVIEW){
            captureStateCallback?.requestAutoAF()
            when(previewType){
                PreviewType.SURFACE_VIEW -> {
                    surfaceView!!.invalidate()
                }

                PreviewType.TEXTURE_VIEW -> {
                    textureView!!.invalidate()
                }

            }
        }
    }

    override fun startPreview(context: Activity) {
        try {

            lastPreviewSize.set(0,0)

            if (!QRConstant.checkCameraPermissions(context)) {
                throw RuntimeException("Need camera Permission")
            }

            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if(cameraManager == null){
                throw RuntimeException(" CameraManager is null ")
            }

            state = State.READY

            cameraCallback.onCameraOpening()

            if(!isSurfaceReady){
                return
            }

            startCamera()

        }catch (e: Exception){
            cameraCallback.onCameraError(e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCamera(){
        if(state != State.READY){
            return
        }

        state = State.OPENING

        cameraId = cameraCallback.selectCamera(cameraManager!!.cameraIdList)

        calibrationCameraDirection()

        val previewSize = getPreviewSize()
        outputImageReader = ImageReader.newInstance(previewSize.x,previewSize.y, ImageFormat.YUV_420_888,1)
        outputImageReader!!.setOnImageAvailableListener(imageReaderCallback,captureHandler)

        val callback = CameraStateCallback(getSurface(),outputImageReader!!.surface)

        cameraManager!!.openCamera(cameraId,callback,mainHandler)

    }

    private fun stopCamera(){
        state = State.PAUSE
        if(captureStateCallback != null){
            captureStateCallback?.close()
            captureStateCallback = null
        }
        if(cameraDevice != null){
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    override fun stopPreview() {
        state = State.DONE
        stopCamera()
        outputImageReader?.close()
    }

    override fun changeFlash(isOpen: Boolean) {
        if(state != State.PREVIEW){
            return
        }
        if(isOpen){
            captureStateCallback?.requestOpenFlash()
        }else{
            captureStateCallback?.requestOffFlash()
        }
    }

    override fun getRotation(): Int {
        return orientation
    }

    override fun getDecodeFrame(): Rect {

        return decodeFrameRect

    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        if(state == State.PREVIEW){
            stopCamera()
            state = State.READY
        }
        if(state == State.READY){
            startCamera()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        isSurfaceReady = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        isSurfaceReady = true
        if(state == State.READY && cameraManager != null){
            startCamera()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        if(state == State.PREVIEW){
            stopCamera()
            state = State.READY
        }
        if(state == State.READY){
            startCamera()
        }
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        isSurfaceReady = false
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        isSurfaceReady = true
        if(state == State.READY && cameraManager != null){
            startCamera()
        }
    }

    private fun calibrationCameraDirection(){
        orientation = calibrationCameraDirection(cameraId,windowManager)
    }

    /**
     * 设置相机方向
     */
    private fun calibrationCameraDirection(cameraId: String, windowManager: WindowManager): Int {
        val deviceOrientation = windowManager.defaultDisplay.rotation
        val cameraCharacteristics = getCameraCharacteristics(cameraId)?:return 0

        val totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation)

        val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        val previewSize = getPreviewSize()

        when(previewType){

            PreviewType.SURFACE_VIEW -> if(surfaceView != null && isSurfaceReady){

                val sizes = map!!.getOutputSizes(SurfaceHolder::class.java)
                val maxSize = getMaxSize(totalRotation, sizes, previewSize.x, previewSize.y)
                if (maxSize != null) {
                    surfaceView.holder.setFixedSize(maxSize.width, maxSize.height)
                }

            }

            PreviewType.TEXTURE_VIEW -> if(textureView != null && isSurfaceReady){

                val sizes = map!!.getOutputSizes(SurfaceTexture::class.java)
                val maxSize = getMaxSize(totalRotation, sizes, previewSize.x, previewSize.y)
                if (maxSize != null) {
                    textureView.surfaceTexture.setDefaultBufferSize(maxSize.width, maxSize.height)
                }

            }

        }
        return totalRotation
    }

    /**
     * 获取最大可用的尺寸
     */
    private fun getMaxSize(totalRotation:Int, sizes: Array<Size>?, inWidth: Int, inHeight: Int): Size? {
        var width = inWidth
        var height = inHeight

        val swapRotation = totalRotation == 90 || totalRotation == 270
        if (swapRotation) {
            val a = width
            width = height
            height = a
        }
        val weight = getWeight(width, height)
        var output: Size? = null
        if (sizes == null)
            return null
        for(size in sizes){
            if(size.width == width && size.height == height){
                return size
            }
        }
        var lastWeight = 0f
        var lastPixels = 0
        for (size in sizes) {
            val newWeight = getWeight(size.width, size.height)
            val newPixels = size.height * size.width
            if (Math.abs(newWeight - weight) < Math.abs(lastWeight - weight)) {//如果比例越接近，那么就优先选择
                output = size
                lastWeight = newWeight
            } else if (Math.abs(newWeight - weight) == Math.abs(lastWeight - weight)) {//如果比例相等，那么就对比分辨率
                if (lastPixels < newPixels) {
                    output = size
                    lastPixels = newPixels
                }
            }
        }
        return output
    }

    /**
     * 获取宽高比例
     */
    private fun getWeight(width: Int, height: Int): Float {
        return 1.0f * width / height
    }

    /**
     * 获取相机连接器
     */
    private fun getCameraCharacteristics(cameraId: String): CameraCharacteristics? {
        return try {
            if (cameraManager == null){
                null
            } else{
                cameraManager?.getCameraCharacteristics(cameraId)
            }
        } catch (e: CameraAccessException) {
            mainHandler.post {
                cameraCallback.onCameraError(e)
            }
            return null
        }

    }

    private fun sensorToDeviceRotation(characteristics: CameraCharacteristics, deviceOrientation: Int): Int {
        val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        val orientation = ORIENTATIONS.get(deviceOrientation)
        return (sensorOrientation + orientation + 360) % 360
    }

    //开启预览
    @Throws(CameraAccessException::class)
    private fun takePreview(outPut: Array<Surface>): CaptureStateCallback {
        // 创建预览需要的CaptureRequest.Builder
        val previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        // 将surface作为CaptureRequest.Builder的目标
        for(surface in outPut){
            previewRequestBuilder.addTarget(surface)
        }
        // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
        val captureStateCallback = CaptureStateCallback(previewRequestBuilder, captureHandler,null)
        cameraDevice!!.createCaptureSession(Arrays.asList(*outPut), captureStateCallback, captureHandler)
        return captureStateCallback
    }

    private fun getSurface(): Surface{
        return if (previewType == PreviewType.SURFACE_VIEW) {
            surfaceView!!.holder.surface
        } else {
            Surface(textureView!!.surfaceTexture)
        }
    }

    //预览拍照的状态回调
    private inner class CaptureStateCallback internal constructor(
            private val previewRequestBuilder: CaptureRequest.Builder, private val captureHandler: Handler,
                                 private var listener: CameraCaptureSession.CaptureCallback?)
        : CameraCaptureSession.StateCallback() {

        private var captureSession: CameraCaptureSession? = null

        init {

            request(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO)

        }

        fun requestAutoAF() {
            request(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            setRepeatingRequest()
        }

        fun requestOpenFlash() {
            request(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            request(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
            setRepeatingRequest()
        }

        fun requestOffFlash() {
            request(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            request(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
            setRepeatingRequest()
        }

        fun requestRedEye() {
            request(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE)
            setRepeatingRequest()
        }

        fun request(key: CaptureRequest.Key<Int>, integer: Int?) {
            previewRequestBuilder.set<Int>(key, integer)
        }

        fun close(){
            captureSession?.close()
            captureSession = null
        }

        override fun onConfigured(session: CameraCaptureSession) {
            //一切就绪时
            if (captureSession != session){
                captureSession = session
            }
            //执行一遍预先设置的参数
            setRepeatingRequest()
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            //出现异常时
            if (captureSession != null){
                captureSession!!.close()
            }
            if (captureSession !== session){
                session.close()
            }
        }

        private fun setRepeatingRequest() {
            if (captureSession == null){
                return
            }
            //执行一遍预先设置的参数
            val request = previewRequestBuilder.build()
            try {
                captureSession!!.setRepeatingRequest(request, listener, captureHandler)
            } catch (e: CameraAccessException) {
                mainHandler.post {
                    cameraCallback.onCameraError(RuntimeException(getCodeError(e.reason), e))
                }
            }

        }

    }

    private inner class CameraStateCallback(vararg outPut: Surface) : CameraDevice.StateCallback() {

        private val outputSurface: Array<Surface> = Array(outPut.size,{ i -> outPut[i] })

        override fun onOpened(camera: CameraDevice) {
            captureHandler.post {
                if (cameraDevice != null) {
                    cameraDevice!!.close()
                    cameraDevice = null
                }
                cameraDevice = camera
                try {
                    val captureStateCallback = takePreview(outputSurface)
                    state = State.PREVIEW
                    this@CameraUtilNew.captureStateCallback = captureStateCallback
                    captureStateCallback.requestRedEye()
                    cameraCallback.onCameraOpen()
                } catch (e: CameraAccessException) {
                    cameraCallback.onCameraError(e)

                }
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            captureHandler.post{
                camera.close()
                if(state != State.DONE){
                    state = State.PAUSE
                }
            }
        }

        /**
         *
         * @param camera
         * @param error @IntDef(value = {
         * CameraDevice.StateCallback.ERROR_CAMERA_IN_USE
         * CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE
         * CameraDevice.StateCallback.ERROR_CAMERA_DISABLED
         * CameraDevice.StateCallback.ERROR_CAMERA_DEVICE
         * CameraDevice.StateCallback.ERROR_CAMERA_SERVICE
         * })
         */
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            state = State.DONE
            mainHandler.post {
                cameraCallback.onCameraError(CameraAccessException(error, "CameraDevice Error, ErrorInfo:${getCodeError(error)},ErrorCode:$error"))
            }
        }
    }

    private inner class ReadImageCallback: ImageReader.OnImageAvailableListener{

        private var resultHandler: Handler? = null
        private var resultCode = 0

        fun ready(handler: Handler,code: Int){
            resultHandler = handler
            resultCode = code
        }

        override fun onImageAvailable(reader: ImageReader?) {

            if (resultHandler != null) {

                captureHandler.post{

                    val image = reader?.acquireLatestImage()

                    if(image != null){

                        val rect = checkDecodeFrame(image)

                        val data = QRConstant.nv21ImageToByteArray(image)

                        val decodeBean = YUVDecodeBean(data,image.width, image.height,rect)
                        val message = resultHandler?.obtainMessage(resultCode, decodeBean)
                        message?.sendToTarget()
                        resultHandler = null

                        image.close()

                    }


                }

            }
        }
    }

    private fun checkDecodeFrame(image: Image): Rect{

        val isChange = orientation % 180 != 0

        val rect = decodeFrameRect

        val size = Size(image.width,image.height)

        if(size.width == lastPreviewSize.x && size.height == lastPreviewSize.y){
            return rect
        }

        val previewFrame = getPreviewFrame()
        val previewSize = getPreviewSize()

        val weight = if(isChange){
            Math.min(1.0f * size.height / previewSize.x , 1.0f * size.width / previewSize.y)
        }else{
            Math.min(1.0f * size.width / previewSize.x , 1.0f * size.height / previewSize.y)
        }

        val width = Math.min(weight * previewFrame.width(),weight * previewFrame.height())

        val offsetY = if(isChange){frameOffset.x * weight}else{frameOffset.y * weight}
        val offsetX = if(isChange){frameOffset.y * weight}else{frameOffset.x * weight}

        val left = (size.width - width)/2 + offsetX
        val top = (size.height - width)/2 + offsetY

        rect.set(left.toInt(),top.toInt(),(left+width).toInt(),(top+width).toInt())

        onDecodeFrameChange(rect)

        lastPreviewSize.set(size.width,size.height)

        return rect

    }

//    override fun onPause() {
//        super.onPause()
//        Log.d("onPause","state:$state")
//        if(state == State.PREVIEW){
//            stopCamera()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Log.d("onResume","state:$state")
//        if(state == State.PAUSE){
//            Log.d("onResume","reopen")
//            state = State.READY
//            startCamera()
//        }
//    }

}