package liang.lollipop.lqr.camera

/**
 * Created by lollipop on 2018/3/26.
 * @author Lollipop
 */
interface CameraCallback {

    fun onCameraOpen()

    fun onCameraError(exception: Exception?)

    fun onCameraOpening()

    fun selectCamera(cameraIds: Array<String>):String

}