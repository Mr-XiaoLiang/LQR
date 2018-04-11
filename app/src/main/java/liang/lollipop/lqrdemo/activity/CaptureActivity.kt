package liang.lollipop.lqrdemo.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_capture.*
import liang.lollipop.lbaselib.base.BaseActivity
import liang.lollipop.lbaselib.util.PermissionsUtil
import liang.lollipop.lqr.LQR
import liang.lollipop.lqr.camera.CameraCallback
import liang.lollipop.lqr.camera.CameraVersion
import liang.lollipop.lqr.decode.CaptureCallback
import liang.lollipop.lqr.decode.CaptureHandler
import liang.lollipop.lqr.encode.QRUtils
import liang.lollipop.lqrdemo.R
import liang.lollipop.lqrdemo.utils.OtherUtils
import liang.lollipop.lqrdemo.utils.SettingsUtil
import java.util.*

class CaptureActivity : BaseActivity(),
        CameraCallback,
        CaptureCallback,
        ValueAnimator.AnimatorUpdateListener,
        Animator.AnimatorListener{

    private lateinit var captureHandler: CaptureHandler

    private var isFlashOpen = false

    private var isPermissionsGrant = false

    private var isDone = false

    private val doorAnimator = ValueAnimator.ofFloat(0F,1F)

    companion object {

        private const val DOOR_ANIMATION_DURATION = 300L
        private const val LOCK_ANIMATION_DURATION = 3000L

        private const val REQUEST_CAMERA = 2

        private const val REQUEST_SHOW = 88

    }


    init {

        doorAnimator.addUpdateListener(this)
        doorAnimator.addListener(this)
        doorAnimator.duration = DOOR_ANIMATION_DURATION

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        val versionCode: Int = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            settingBtn.visibility = View.GONE
            SettingsUtil.CAMERA_MODEL_OLD
        }else{
            settingBtn.visibility = View.VISIBLE
            SettingsUtil.getCameraModel(this)
        }
        val version = when(versionCode){
            SettingsUtil.CAMERA_MODEL_OLD -> {
                CameraVersion.OLD
            }

            SettingsUtil.CAMERA_MODEL_NEW -> {
                CameraVersion.LOLLIPOP
            }

            else -> {
                CameraVersion.AUTO
            }

        }
        captureHandler = LQR.capture(version,surfaceView,this,finderView,this)
        finderView.setOnClickListener(this)
        flashBtn.setOnClickListener(this)
        settingBtn.setOnClickListener(this)

    }

    override fun onPermissionsGrant(requestCode: Int) {
        super.onPermissionsGrant(requestCode)

        when(requestCode){
            REQUEST_CAMERA -> {
                isPermissionsGrant = true
                captureHandler.onStart(this)
            }
        }
    }

    override fun onPermissionsUnAllow(requestCode: Int, permissions: ArrayList<String>) {
        super.onPermissionsUnAllow(requestCode, permissions)
        popPermissionsDialog(getString(R.string.request_camera_msg),
                getString(R.string.request_camera_title),
                getString(R.string.request_camera_yes),
                getString(R.string.request_camera_no))
    }

    override fun onClick(v: View?) {
        when(v){

            flashBtn -> {
                isFlashOpen = !isFlashOpen
                captureHandler.changeFlash(isFlashOpen)
                flashBtn.setImageResource(if(isFlashOpen){ R.drawable.ic_flash_on_white_24dp }else{ R.drawable.ic_flash_off_white_24dp })
            }

            finderView -> {
                captureHandler.requestFocus()
            }

            settingBtn -> {

                alert().setTitle(getString(R.string.this_camera_version)+getCameraVersionName())
                        .setMessage(getString(R.string.msg_change_version))
                        .setPositiveButton(R.string.new_api) { dialog, _ ->
                            setCameraModel(SettingsUtil.CAMERA_MODEL_NEW,dialog)
                        }
                        .setNeutralButton(R.string.auto){ dialog, _ ->
                            setCameraModel(SettingsUtil.CAMERA_MODEL_AUTO,dialog)
                        }
                        .setNegativeButton(R.string.old_api){ dialog, _ ->
                            setCameraModel(SettingsUtil.CAMERA_MODEL_OLD,dialog)
                        }
                        .show()

            }

        }
    }

    private fun getCameraVersionName(): String{
        return when(SettingsUtil.getCameraModel(this)){
            SettingsUtil.CAMERA_MODEL_OLD -> {
                getString(R.string.old_api)
            }

            SettingsUtil.CAMERA_MODEL_NEW -> {
                getString(R.string.new_api)
            }

            else -> {
                getString(R.string.auto)
            }

        }
    }

    private fun setCameraModel(value:Int,dialogInterface: DialogInterface){
        SettingsUtil.setCameraModel(this,value)
        dialogInterface.dismiss()
        onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        if(isDone){
            return
        }
        initDoor()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            isPermissionsGrant = PermissionsUtil.checkPermission(this,PermissionsUtil.CAMERA_)
            if(!isPermissionsGrant){
                requestPermissions(arrayOf(PermissionsUtil.CAMERA_), REQUEST_CAMERA)
                return
            }
        }
        captureHandler.onStart(this)

    }

    override fun onStop() {
        super.onStop()
        if(isPermissionsGrant && !isDone){
            captureHandler.onStop()
        }
    }

    override fun onResume() {
        super.onResume()
        if(isPermissionsGrant && !isDone){
            captureHandler.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if(isPermissionsGrant && !isDone){
            captureHandler.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        captureHandler.onDestroy()
    }

    override fun onCameraOpen() {
        openDoor()
    }

    override fun onCameraError(exception: Exception?) {
        alert(getString(R.string.camera_error), exception?.localizedMessage?:"")
        exception?.printStackTrace()
    }

    override fun onCameraOpening() {
        openStartLock()
    }

    override fun onSuccess(result: Result, barcode: Bitmap?, scaleFactor: Float) {
        //如果使用新版API，在出现Dialog之后，相机连接会丢失，暂未找到原因，因此此处采用跳转新页面的方式。
        //在新的页面展示结果，当跳转回来时，重新建立相机连接。
        //如果仅使用老版API，则不用跳转到新的页面

        if(intent.action == "com.google.zxing.client.android.SCAN"){
            val resultIntent = Intent()
            resultIntent.putExtra("SCAN_RESULT",result.text)
            resultIntent.putExtra("SCAN_RESULT_FORMAT",result.barcodeFormat.toString())
            if(barcode != null){
                resultIntent.putExtra("SCAN_RESULT_BYTES",OtherUtils.Bitmap2Bytes(barcode))
            }
            resultIntent.putExtra("SCAN_RESULT_ORIENTATION",finderView.getRotetion())
            resultIntent.putExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL","")
            setResult(Activity.RESULT_OK,resultIntent)
            onBackPressed()
        }else{
            val newIntent = Intent(this, QRResultActivity::class.java)
            newIntent.putExtra(QRResultActivity.ARG_IMAGE_ROTETION,finderView.getRotetion())
            if(barcode != null){
                newIntent.putExtra(QRResultActivity.ARG_IMAGE_DATA, OtherUtils.Bitmap2Bytes(barcode))
            }
            newIntent.putExtra(QRResultActivity.ARG_TEXT_VALUE,result.text)
            startActivityForResult(newIntent, REQUEST_SHOW)
        }

    }

    override fun selectCamera(cameraIds: Array<String>): String {
        if(cameraIds.isEmpty()){
            return "0"
        }
        return cameraIds[0]
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        when(animation){

            doorAnimator -> captureHandler.post{

                val value = doorAnimator.animatedValue as Float

                leftDoorView.translationX = -1F * leftDoorView.width * value
                rightDoorView.translationX = rightDoorView.width * value
                lockView.translationX = lockView.right * value

            }

        }
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }

    override fun onAnimationEnd(animation: Animator?) {

        when(animation){

            doorAnimator -> captureHandler.post{
                leftDoorView.visibility = View.INVISIBLE
                rightDoorView.visibility = View.INVISIBLE
                lockView.visibility = View.INVISIBLE
                lockView.clearAnimation()
            }

        }
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationStart(animation: Animator?) {

        when(animation){

            doorAnimator -> captureHandler.post{

                lockView.clearAnimation()

            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){

            REQUEST_SHOW -> if(resultCode == QRResultActivity.RESULT_EXIT){
                isDone = true
                onBackPressed()
            }

        }
    }

    private fun openStartLock(){
        lockView.clearAnimation()
        val rotationAnimation = RotateAnimation(0F, 360F,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
        rotationAnimation.duration = LOCK_ANIMATION_DURATION
        rotationAnimation.repeatMode = Animation.INFINITE
        lockView.startAnimation(rotationAnimation)
    }

    private fun openDoor(){
        doorAnimator.cancel()
        doorAnimator.start()
    }

    private fun initDoor(){
        doorAnimator.cancel()
        leftDoorView.translationX = 0F
        leftDoorView.visibility = View.VISIBLE
        rightDoorView.translationX = 0F
        rightDoorView.visibility = View.VISIBLE
        lockView.translationX = 0F
        lockView.rotation = 0F
        lockView.visibility = View.VISIBLE
    }

}
