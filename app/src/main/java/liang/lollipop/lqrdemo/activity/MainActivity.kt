package liang.lollipop.lqrdemo.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition
import com.liang.lollipop.lcrop.LCropUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_main.*
import kotlinx.android.synthetic.main.content_main.*
import liang.lollipop.lbaselib.base.BaseActivity
import liang.lollipop.lbaselib.util.PermissionsUtil
import liang.lollipop.lbaselib.util.TaskUtils
import liang.lollipop.lcolorpalette.ColorPickerDialog
import liang.lollipop.lcolorpalette.drawable.CircleBgDrawable
import liang.lollipop.lqr.LQR
import liang.lollipop.lqr.callback.BitmapCallback
import liang.lollipop.lqr.callback.ImageViewCallback
import liang.lollipop.lqrdemo.R
import liang.lollipop.lqrdemo.fragment.SaveQRDialogFragment
import liang.lollipop.lqrdemo.utils.OtherUtils
import liang.lollipop.lqrdemo.utils.SettingsUtil


/**
 * 主页，创建二维码的Activity
 * @author Lollipop
 */
class MainActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener, SaveQRDialogFragment.SaveQRCallback{

    private val darkColorDrawable = CircleBgDrawable()
    private val lightColorDrawable = CircleBgDrawable()

    private val requestOptions = RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)

    private var isPermissionsGrant = false

    private var lastQRValue = ""

    companion object {

        private const val REQUEST_BG = 8

        private const val REQUEST_EXTERNAL_STORAGE = 7

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbar(toolbar)
        initView()

    }

    private fun initView(){

        ColorPickerDialog.init(ContextCompat.getColor(this,R.color.colorPrimary),ContextCompat.getColor(this,R.color.colorAccent))

        darkColorShow.setImageDrawable(darkColorDrawable)
        lightColorShow.setImageDrawable(lightColorDrawable)

        showDarkColor(SettingsUtil.getDarkColor(this))
        showLightColor(SettingsUtil.getLightColor(this))

        val isMiniModel = SettingsUtil.isMiniModel(this)
        openBgSwitch.isChecked = isMiniModel
        bgStatusChange(isMiniModel)

        onBGChange()

        createBtn.setOnClickListener(this)
        darkColorBtn.setOnClickListener(this)
        lightColorBtn.setOnClickListener(this)
        openBgSwitch.setOnCheckedChangeListener(this)
        bgImageBtn.setOnClickListener(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.menu_download -> {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    isPermissionsGrant = PermissionsUtil.checkPermission(this,PermissionsUtil.CAMERA_)
                    if(!isPermissionsGrant){
                        requestPermissions(arrayOf(PermissionsUtil.CAMERA_), REQUEST_EXTERNAL_STORAGE)
                        return true
                    }
                }
                val value = textInputEdit.text.toString().trim()

                if(TextUtils.isEmpty(value)){
                    return true
                }
                SaveQRDialogFragment.newInstance(qrImageView.width).show(supportFragmentManager,"SaveQRDialog")
                return true

            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {

        when(v){

            darkColorBtn -> {

                ColorPickerDialog().setColor(SettingsUtil.getDarkColor(this)).setColorSelectedListener(
                        object : ColorPickerDialog.OnColorSelectedListener{
                            override fun onColorSelected(dialog: ColorPickerDialog, color: Int) {
                                setDarkColor(color)
                                dialog.dismiss()
                            }
                        }).show(supportFragmentManager,"darkColor")

            }

            lightColorBtn -> {

                ColorPickerDialog().setColor(SettingsUtil.getLightColor(this)).setColorSelectedListener(
                        object : ColorPickerDialog.OnColorSelectedListener{
                            override fun onColorSelected(dialog: ColorPickerDialog, color: Int) {
                                setLightColor(color)
                                dialog.dismiss()
                            }
                        }).show(supportFragmentManager,"lightColor")

            }

            bgImageBtn -> {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    isPermissionsGrant = PermissionsUtil.checkPermission(this,PermissionsUtil.CAMERA_)
                    if(!isPermissionsGrant){
                        requestPermissions(arrayOf(PermissionsUtil.CAMERA_), REQUEST_EXTERNAL_STORAGE)
                        return
                    }
                }
                LCropUtil.selectPhotos(this,REQUEST_BG)

            }

            createBtn -> {

                onBGChange()
                val value = textInputEdit.text.toString().trim()

                if(TextUtils.isEmpty(value)){
                    return
                }

                onLoad()

                createQR(value)
            }

        }

    }

    override fun okToSave(width: Int) {
        onLoad()

        createBitmap(width)
    }

    private fun createBitmap(width: Int){
        val isMini = SettingsUtil.isMiniModel(this)

        if(!isMini){
            createQRBitmap(null,isMini,width)
            return
        }
        val imageFile = OtherUtils.getTempImageFile(this)
        if(!imageFile.exists()){
            onLoadEnd()
            alert(getString(R.string.title_no_bg),getString(R.string.msg_no_bg))
            return
        }
        glide.asBitmap()
                .load(OtherUtils.getTempImagePath(this))
                .apply(RequestOptions().centerCrop().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
                .listener(object :RequestListener<Bitmap>{
                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        onLoadEnd()
                        alert(getString(R.string.load_image_error),e?.localizedMessage?:"")
                        return false
                    }
                })
                .into(object : SimpleTarget<Bitmap>(width,width){

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                createQRBitmap(resource, isMini, width)

            }

        })
    }

    private fun saveBitmap(bitmap: Bitmap){

        TaskUtils.addUITask(object :TaskUtils.UICallback<Boolean,Bitmap>{
            override fun onSuccess(result: Boolean) {
                onLoadEnd()
                alert().setTitle(getString(R.string.title_qr_saved))
                        .setMessage(String.format(getString(R.string.msg_qr_saved),OtherUtils.getAppDir().absolutePath))
                        .setPositiveButton("打开"){ dialog, _ ->
                            OtherUtils.openAssignFolder(this@MainActivity,OtherUtils.getAppDir().absolutePath)
                            dialog.dismiss()
                        }
                        .show()
            }

            override fun onError(e: Exception, code: Int, msg: String) {
                onLoadEnd()
                bitmap.recycle()
                alert(getString(R.string.error_save_qr),e.localizedMessage)
                e.printStackTrace()
            }

            override fun onBackground(args: Bitmap?): Boolean {
                OtherUtils.saveBitmap(args!!)
                args.recycle()
                return true
            }
        },bitmap)

    }

    private fun createQRBitmap(resource: Bitmap?,isMini: Boolean,width: Int){
        val dark = SettingsUtil.getDarkColor(this)
        val light = SettingsUtil.getLightColor(this)
        LQR.with(lastQRValue)
                .darkColor(dark)
                .lightColor(light)
                .size(width)
                .miniQR(isMini)
                .ansyTo(resource,object : BitmapCallback{
                    override fun onSuccess(result: Bitmap) {

                        saveBitmap(result)

                    }

                    override fun onError(e: Exception?) {

                        onLoadEnd()
                        alert(getString(R.string.error_save_qr),e?.localizedMessage?:"")

                    }

                })
    }

    private fun createQR(value: String){
        lastQRValue = value
        val dark = SettingsUtil.getDarkColor(this)
        val light = SettingsUtil.getLightColor(this)
        val isMini = SettingsUtil.isMiniModel(this)
        if(!isMini){
            qrImageView.setImageResource(0)
            LQR.with(value)
                    .darkColor(dark)
                    .lightColor(light)
                    .miniQR(isMini)
                    .ansyTo(qrImageView,object : ImageViewCallback {
                        override fun onSuccess(result: ImageView) {
                            onLoadEnd()
                        }

                        override fun onError(e: Exception?) {
                            onLoadEnd()
                            alert(getString(R.string.error_title),getString(R.string.create_qr_error)+e?.localizedMessage)
                        }
                    })
            return
        }
        val imageFile = OtherUtils.getTempImageFile(this)
        if(!imageFile.exists()){
            onLoadEnd()
            alert(getString(R.string.title_no_bg),getString(R.string.msg_no_bg))
            return
        }
        glide.load(OtherUtils.getTempImagePath(this))
                .apply(RequestOptions().centerCrop().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
                .listener(object :RequestListener<Drawable>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        onLoadEnd()
                        alert(getString(R.string.load_image_error),e?.localizedMessage?:"")
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                }).into(object :ViewTarget<ImageView,Drawable>(qrImageView){

                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {

                        this.view.setImageDrawable(resource)

                        LQR.with(value)
                                .darkColor(dark)
                                .lightColor(light)
                                .miniQR(isMini)
                                .ansyTo(qrImageView,object : ImageViewCallback {
                                    override fun onSuccess(result: ImageView) {
                                        onLoadEnd()
                                    }

                                    override fun onError(e: Exception?) {
                                        onLoadEnd()
                                        alert(getString(R.string.error_title),getString(R.string.create_qr_error)+e?.localizedMessage)
                                    }
                                })

                    }


                })

    }

    override fun onPermissionsGrant(requestCode: Int) {
        super.onPermissionsGrant(requestCode)
        when(requestCode){
            REQUEST_EXTERNAL_STORAGE -> {
                isPermissionsGrant = true
            }
        }
    }

    override fun onPermissionsUnAllow(requestCode: Int, permissions: ArrayList<String>) {
        super.onPermissionsUnAllow(requestCode, permissions)
        popPermissionsDialog(getString(R.string.request_external_storage_msg),
                getString(R.string.request_external_storage_title),
                getString(R.string.request_camera_yes),
                getString(R.string.request_camera_no))
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when(buttonView){

            openBgSwitch -> {
                bgStatusChange(isChecked)
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){

            REQUEST_BG -> {
                val urls = LCropUtil.getData(resultCode,data)
                if(urls.isNotEmpty()){
                    onLoad()
                    TaskUtils.addUITask(object : TaskUtils.UICallback<Boolean,String>{
                        override fun onSuccess(result: Boolean) {
                            onLoadEnd()
                            if(result){
                                onBGChange()
                            }else{
                                alert(getString(R.string.error_title),getString(R.string.error_msg_save))
                            }
                        }

                        override fun onError(e: Exception, code: Int, msg: String) {
                            onLoadEnd()
                            alert(getString(R.string.error_title),getString(R.string.error_msg_save)+e.localizedMessage)
                        }

                        override fun onBackground(args: String?): Boolean {
                            return try {
                                OtherUtils.copyFile(args!!,OtherUtils.getTempImagePath(this@MainActivity))
                                true
                            }catch (e: Exception){
                                false
                            }
                        }

                    },urls[0])
                }
            }

            LCropUtil.CROP_REQUEST -> {

                onBGChange()

            }

        }
    }

    private fun setDarkColor(value: Int){
        showDarkColor(value)
        SettingsUtil.putDarkColor(this,value)
    }

    private fun showDarkColor(value: Int){
        darkColorDrawable.setColor(value)
        darkColorValue.text = OtherUtils.getColorValue(value)
    }

    private fun setLightColor(value: Int){
        showLightColor(value)
        SettingsUtil.putLightColor(this,value)
    }

    private fun showLightColor(value: Int){
        lightColorDrawable.setColor(value)
        lightColorValue.text = OtherUtils.getColorValue(value)
    }

    private fun bgStatusChange(isOpen: Boolean){

        bgImageBtn.isEnabled = isOpen
        SettingsUtil.setMiniModel(this,isOpen)

    }

    private fun onBGChange(){
        val imageFile = OtherUtils.getTempImageFile(this)
        val imagePath = imageFile.absolutePath
        if(!imageFile.exists()){
            return
        }
        glide.load(imagePath).apply(requestOptions).into(qrImageView)
        glide.load(imagePath).apply(requestOptions).into(bgImageShow)
    }

}
