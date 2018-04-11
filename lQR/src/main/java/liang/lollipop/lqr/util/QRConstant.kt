package liang.lollipop.lqr.util

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.media.Image
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import java.util.ArrayList

/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 二维码相关的常量
 */
object QRConstant {

    /**
     * 二维码扫描框的动画刷新时间
     * 单位为ms毫秒
     */
    var FINDER_ANIMATION_DELAY = 10L

    fun checkPermissions(activity: Activity, vararg permissions: String): Boolean {
        val needRequestPermissonList = findDeniedPermissions(activity, *permissions)
        return needRequestPermissonList.isEmpty()
    }

    fun checkCameraPermissions(activity: Activity): Boolean {
        return checkPermissions(activity,Manifest.permission.CAMERA)
    }

    /**
     * 获取权限集中需要申请权限的列表
     */
    private fun findDeniedPermissions(activity: Activity, vararg permissions: String): List<String> {
        val needRequestPermissonList = ArrayList<String>()
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                            activity, perm)) {
                needRequestPermissonList.add(perm)
            }
        }
        return needRequestPermissonList
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun yuvImageToByteArray(image: Image): ByteArray {

        assert(image.format == ImageFormat.YUV_420_888)

        val width = image.width
        val height = image.height

        val planes:Array<Image.Plane> = image.planes
        val result = ByteArray(width * height * 3 / 2)

        var stride = planes[0].rowStride
        if (stride == width) {
            planes[0].buffer.get(result, 0, width)
        } else {
            for (row in 0 until height) {
                planes[0].buffer.position(row*stride)
                planes[0].buffer.get(result, row*width, width)
            }
        }

        stride = planes[1].rowStride
        assert (stride == planes[2].rowStride)
        val rowBytesCb = ByteArray(stride)
        val rowBytesCr = ByteArray(stride)

        for (row in 0 until  height/2) {
            val rowOffset = width*height + width/2 * row
            planes[1].buffer.position(row*stride)
            planes[1].buffer.get(rowBytesCb, 0, width/2)
            planes[2].buffer.position(row*stride)
            planes[2].buffer.get(rowBytesCr, 0, width/2)

            for (col in 0 until width/2) {
                result[rowOffset + col*2] = rowBytesCr[col]
                result[rowOffset + col*2 + 1] = rowBytesCb[col]
            }
        }
        return result
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun isImageFormatSupported(image: Image): Boolean {
        val format = image.format
        when (format) {
            ImageFormat.YUV_420_888, ImageFormat.NV21, ImageFormat.YV12 -> return true
        }
        return false
    }

    private enum class ColorFormat {
        I420,NV21
    }

    fun nv21ImageToByteArray(image: Image): ByteArray{
        return getDataFromImage(image,ColorFormat.NV21)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getDataFromImage(image: Image, colorFormat: ColorFormat): ByteArray {
        if (!isImageFormatSupported(image)) {
            throw RuntimeException("can't convert Image to byte array, format " + image.format)
        }
        val crop = image.cropRect
        val format = image.format
        val width = crop.width()
        val height = crop.height()
        val planes = image.planes
        val data = ByteArray(width * height * ImageFormat.getBitsPerPixel(format) / 8)
        val rowData = ByteArray(planes[0].rowStride)
        var channelOffset = 0
        var outputStride = 1
        for (i in planes.indices) {
            when (i) {
                0 -> {
                    channelOffset = 0
                    outputStride = 1
                }
                1 -> when (colorFormat) {
                    ColorFormat.I420 -> {
                        channelOffset = width * height
                        outputStride = 1
                    }

                    ColorFormat.NV21 -> {
                        channelOffset = width * height + 1
                        outputStride = 2
                    }

                }
                2 -> when (colorFormat){
                    ColorFormat.I420 -> {
                        channelOffset = (width.toDouble() * height.toDouble() * 1.25).toInt()
                        outputStride = 1
                    }

                    ColorFormat.NV21 -> {
                        channelOffset = width * height
                        outputStride = 2
                    }
                }
            }
            val buffer = planes[i].buffer
            val rowStride = planes[i].rowStride
            val pixelStride = planes[i].pixelStride
            val shift = if (i == 0) 0 else 1
            val w = width shr shift
            val h = height shr shift
            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
            for (row in 0 until h) {
                val length: Int
                if (pixelStride == 1 && outputStride == 1) {
                    length = w
                    buffer.get(data, channelOffset, length)
                    channelOffset += length
                } else {
                    length = (w - 1) * pixelStride + 1
                    buffer.get(rowData, 0, length)
                    for (col in 0 until w) {
                        data[channelOffset] = rowData[col * pixelStride]
                        channelOffset += outputStride
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
        }
        return data
    }

    const val SCAN_CAMERA_ID = "SCAN_CAMERA_ID"

    const val SCAN_FORMATS = "SCAN_FORMATS"

}