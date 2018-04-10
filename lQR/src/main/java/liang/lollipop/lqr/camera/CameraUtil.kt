package liang.lollipop.lqr.camera

import android.app.Activity
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.util.Log
import android.util.SparseIntArray
import android.view.*


/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 相机工具类的抽象接口
 */
abstract class CameraUtil (protected val previewType: PreviewType,
                          protected val textureView: TextureView?,
                          protected val surfaceView: SurfaceView?,
                          protected val cameraCallback: CameraCallback): SurfaceHolder.Callback,TextureView.SurfaceTextureListener {

    protected var state = State.DONE

    protected var orientation: Int = 0

    protected val frameOffset = Point(0,0)

    private var maxFrameWidth = -1

    private var frameWeight = 1.0f * 2 / 3

    protected var isSurfaceReady = false

    lateinit var windowManager: WindowManager

    var onDecodeFrameChangeCallback: OnDecodeFrameChangeCallback? = null

    protected val decodeFrameRect = Rect(0,0,0,0)
    private val previewFrameRect = Rect(0,0,0,0)

    enum class PreviewType{
        SURFACE_VIEW,TEXTURE_VIEW
    }

    enum class State {
        PREVIEW,
        PAUSE,
        READY,
        OPENING,
        DONE
    }

    init {

        when(previewType){

            PreviewType.SURFACE_VIEW -> {
                surfaceView?.holder?.addCallback(this)
            }

            PreviewType.TEXTURE_VIEW -> {
                textureView?.surfaceTextureListener = this
            }

        }

    }

    companion object {

        //手机方向
        val ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 0)
            append(Surface.ROTATION_90, 90)
            append(Surface.ROTATION_180, 180)
            append(Surface.ROTATION_270, 270)
        }

    }

    fun frameOffset(x:Int , y:Int): CameraUtil{
        frameOffset.set(x, y)
        return this
    }

    fun maxFrame(width:Int): CameraUtil{
        maxFrameWidth = width
        return this
    }

    protected fun getPreviewSize(): Point{
        return when(previewType){

            PreviewType.SURFACE_VIEW -> {
                Point(surfaceView!!.width,surfaceView.height)
            }

            PreviewType.TEXTURE_VIEW -> {
                Point(textureView!!.width,textureView.height)
            }

        }
    }

    abstract fun requestPreviewFrame(handler: Handler, code:Int)

    abstract fun requestFocus(handler: Handler, code:Int)

    abstract fun startPreview(context: Activity)

    abstract fun stopPreview()

    abstract fun changeFlash(isOpen: Boolean)

    abstract fun getRotation(): Int

    abstract fun getDecodeFrame(): Rect

    open fun getPreviewFrame(): Rect{
        val rect = previewFrameRect

        val screenResolution = getPreviewSize()
        var width = Math.min(screenResolution.x * frameWeight,screenResolution.y * frameWeight).toInt()
        if(maxFrameWidth > 0){
            width = Math.min(width,maxFrameWidth)
        }
        val leftOffset = ((screenResolution.x - width) / 2) + frameOffset.x
        val topOffset = (screenResolution.y - width) / 2 + frameOffset.y

        rect.set(leftOffset, topOffset, leftOffset + width, topOffset + width)

        return rect
    }

    protected fun onDecodeFrameChange(frame: Rect){
        Log.d("buildLuminanceSource","frame:$frame")
        onDecodeFrameChangeCallback?.onDecodeFrameChange(frame)
    }

    interface OnDecodeFrameChangeCallback{
        fun onDecodeFrameChange(frame: Rect)
    }

    open fun onResume(){

    }

    open fun onPause(){

    }

}