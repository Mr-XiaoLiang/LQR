package liang.lollipop.lqrdemo.utils

import android.app.Activity
import android.content.*
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import liang.lollipop.lqrdemo.R
import liang.lollipop.lqrdemo.bean.AppInfo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


/**
 * 杂乱的工具
 * @author Lollipop
 */
object OtherUtils{


    fun Bitmap2Bytes(bm: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    fun Bytes2Bimap(b: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(b, 0, b.size)
    }

    /**
     * 实现文本复制功能
     * @param content
     */
    fun copy(content: String, context: Context) {
        // 得到剪贴板管理器
        val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = ClipData.newPlainText(context.getString(R.string.app_name),content.trim())
        cmb.primaryClip = data
    }

    fun getColorValue(color: Int): String {
        if (color == 0) {
            return "#000000"
        }
        val red = Color.red(color).formatNum()
        val green = Color.green(color).formatNum()
        val blue = Color.blue(color).formatNum()
        return "#$red$green$blue"
    }

    private fun Int.formatNum(): String{
        val numStr = Integer.toHexString(this).toUpperCase()
        return if(this < 0x10){ "0$numStr" }else{ numStr }
    }

    fun getAppDir():File{
        val dir = File(Environment.getExternalStorageDirectory(),"LQR")
        if(!dir.exists() || !dir.isDirectory){
            dir.mkdirs()
        }
        return dir
    }

    fun getLogDir():File{
        val dir = File(getAppDir(),"log")
        if(!dir.exists() || !dir.isDirectory){
            dir.mkdirs()
        }
        return dir
    }

    fun getBgImagePath(context: Context): String{
        val path = File(getAppDir(),"qrBg.png")
        return path.absolutePath
    }

    fun getBgImageUri(context: Context): Uri{
        return getUri(context, getBgImagePath(context))
    }

    fun getTempImageFile(context: Context): File{
        return File(getAppDir(),"temp.png")
    }

    fun getTempImagePath(context: Context): String{
        return getTempImageFile(context).absolutePath
    }

    fun getTempImageUri(context: Context): Uri{
        return getUri(context, getTempImagePath(context))
    }

    fun getUri(context: Context, path: String): Uri {
        val resultUri: Uri?
        resultUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //安卓N以后，文件管理高度私有化，
            // 如果跨应用传递地址，需要使用ContentProvider或FileProvider
            val contentValues = ContentValues(1)
            contentValues.put(MediaStore.Images.Media.DATA, path)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            Uri.parse(path)
        }
        if (resultUri == null) {
            throw RuntimeException("Uri is null")
        }
        return resultUri
    }

    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    fun copyFile(oldPath: String, newPath: String) {
        try {
            var bytesum = 0
//            var byteread = 0
            val oldfile = File(oldPath)
            if (oldfile.exists()) { //文件存在时
                val inStream = FileInputStream(oldPath) //读入原文件
                val newFile = File(newPath)
                if(newFile.exists() && newFile.isFile){
                    newFile.delete()
                }
                val fs = FileOutputStream(newFile)
                val buffer = ByteArray(1024)
                val length: Int
                var byteread = inStream.read(buffer)
                while (byteread != -1) {
                    bytesum += byteread //字节数 文件大小
                    println(bytesum)
                    fs.write(buffer, 0, byteread)
                    byteread = inStream.read(buffer)
                }
                inStream.close()
            }
        } catch (e: Exception) {
            println("复制单个文件操作出错")
            e.printStackTrace()

        }

    }

    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    fun copyFolder(oldPath: String, newPath: String) {

        try {
            File(newPath).mkdirs() //如果文件夹不存在 则建立新文件夹
            val a = File(oldPath)
            val file = a.list()
            for (i in file.indices) {
                val temp = if (oldPath.endsWith(File.separator)) {
                    File(oldPath + file[i])
                } else {
                    File(oldPath + File.separator + file[i])
                }

                if (temp.isFile) {
                    val input = FileInputStream(temp)
                    val output = FileOutputStream(newPath + "/" +
                            temp.name.toString())
                    val b = ByteArray(1024 * 5)
                    var len: Int = input.read(b)
                    while (len != -1) {
                        output.write(b, 0, len)
                        len = input.read(b)
                    }
                    output.flush()
                    output.close()
                    input.close()
                }
                if (temp.isDirectory) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i])
                }
            }
        } catch (e: Exception) {
            println("复制整个文件夹内容操作出错")
            e.printStackTrace()

        }

    }

    fun saveBitmap(bitmap: Bitmap){
        val file = File(getAppDir(), "${System.currentTimeMillis()}.jpg")
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        } finally {
            out?.flush()
            out?.close()
        }
    }

    fun openAssignFolder(context: Context,path: String) {
        val file = File(path)
        if (!file.exists()) {
            return
        }
        val intent = Intent(ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(Uri.fromFile(file), "file/*")
        try {
//            context.startActivity(intent)
            context.startActivity(Intent.createChooser(intent, "选择浏览工具"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }

    private fun hintLauncherIcon(context: Activity,componentName: ComponentName){
        val packageManager = context.packageManager
        packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                PackageManager.DONT_KILL_APP)
    }

    private fun showLauncherIcon(context: Activity,componentName: ComponentName){
        val packageManager = context.packageManager
        packageManager.setComponentEnabledSetting(
                context.componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP)
    }

    fun changeLauncherIconStatus(context: Activity,componentName: ComponentName,isShow: Boolean){
        if(isShow){
            showLauncherIcon(context,componentName)
        }else{
            hintLauncherIcon(context,componentName)
        }
    }

    fun changeLauncherIconStatus(context: Activity, isShow: Boolean){
        if(isShow){
            showLauncherIcon(context,context.componentName)
        }else{
            hintLauncherIcon(context,context.componentName)
        }
    }

    /**
     * 获取非系统应用信息列表
     */
    fun getAppList(context: Activity): ArrayList<AppInfo> {
        val pm = context.packageManager
        // Return a List of all packages that are installed on the device.
        val packages = pm.getInstalledPackages(0)

        val appList = ArrayList<AppInfo>(packages.size)

        for (packageInfo in packages) {

            appList.add(AppInfo.create(packageInfo,pm))

        }
        return appList
    }

}