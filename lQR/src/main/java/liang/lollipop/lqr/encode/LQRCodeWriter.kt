package liang.lollipop.lqr.encode

import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.Writer
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.decoder.Version
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode

/**
 * Created by lollipop on 2018/3/15.
 * @author Lollipop
 * 定制输出效果的QRCode输出类
 */
class LQRCodeWriter(private val writerType: WriterType = WriterType.DEFAULT) : Writer {

    companion object {

        private const val TAG = "LQRCodeWriter"

        private const val QUIET_ZONE_SIZE = 4

    }

    enum class WriterType{

        DEFAULT,MINI

    }

    @Throws(WriterException::class)
    override fun encode(contents: String, format: BarcodeFormat, width: Int, height: Int): BitMatrix {

        return encode(contents, format, width, height, null)
    }

    @Throws(WriterException::class)
    override fun encode(contents: String,
                        format: BarcodeFormat,
                        width: Int,
                        height: Int,
                        hints: Map<EncodeHintType, *>?): BitMatrix {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Requested dimensions are too small: $width x $height")
        }
        val bean = encodeTo(contents, format, width, height, hints)
        return renderResult(bean.qrCode, bean.width, bean.height, bean.quietZone)
    }

    @Throws(WriterException::class)
    fun encode2(contents: String,
                        format: BarcodeFormat,
                        width: Int,
                        height: Int,
                        hints: Map<EncodeHintType, *>? = null): LBitMatrix {
        val bean = encodeTo(contents, format, width, height, hints)
        return renderResultLQR(bean.qrCode, bean.width, bean.height, bean.quietZone)
    }

    @Throws(WriterException::class)
    private fun encodeTo(contents: String,
                         format: BarcodeFormat,
                         width: Int,
                         height: Int,
                         hints: Map<EncodeHintType, *>? = null): CodeBean {

        if (contents.isEmpty()) {
            throw IllegalArgumentException("Found empty contents")
        }

        if (format != BarcodeFormat.QR_CODE) {
            throw IllegalArgumentException("Can only encode QR_CODE, but got $format")
        }

        var errorCorrectionLevel = ErrorCorrectionLevel.L
        var quietZone = QUIET_ZONE_SIZE
        if (hints != null) {
            if (hints.containsKey(EncodeHintType.ERROR_CORRECTION)) {
                errorCorrectionLevel = ErrorCorrectionLevel.valueOf(hints[EncodeHintType.ERROR_CORRECTION].toString())
            }
            if (hints.containsKey(EncodeHintType.MARGIN)) {
                quietZone = Integer.parseInt(hints[EncodeHintType.MARGIN].toString())
            }
        }
        Log.d(TAG,"Encoder start:${System.currentTimeMillis()}")
        val code = Encoder.encode(contents, errorCorrectionLevel, hints)
        Log.d(TAG,"Encoder end:${System.currentTimeMillis()}")
        return CodeBean(code, width, height, quietZone)
    }

    private class CodeBean(val qrCode: QRCode,val width: Int,val height: Int,val quietZone: Int)

    // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    private fun renderResult(code: QRCode, width: Int, height: Int, quietZone: Int): BitMatrix {
        val input = code.matrix ?: throw IllegalStateException()
        val inputWidth = input.width
        val inputHeight = input.height
        val qrWidth = inputWidth + quietZone * 2
        val qrHeight = inputHeight + quietZone * 2
        val outputWidth = Math.max(width, qrWidth)
        val outputHeight = Math.max(height, qrHeight)

        val multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight)
        // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
        // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
        // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
        // handle all the padding from 100x100 (the actual QR) up to 200x160.
        val leftPadding = (outputWidth - inputWidth * multiple) / 2
        val topPadding = (outputHeight - inputHeight * multiple) / 2

        val output = BitMatrix(outputWidth, outputHeight)

        var inputY = 0
        var outputY = topPadding

        while (inputY < inputHeight) {
            // Write the contents of this row of the barcode
            var inputX = 0
            var outputX = leftPadding
            while (inputX < inputWidth) {
                if (input.get(inputX, inputY).toInt() == 1) {

                    output.setRegion(outputX, outputY, multiple, multiple)

                }
                inputX++
                outputX += multiple
            }
            inputY++
            outputY += multiple
        }

        return output
    }

    private fun renderResultLQR(code: QRCode, w: Int, h: Int, quietZone: Int): LBitMatrix {
        Log.d(TAG,"render start:${System.currentTimeMillis()}")
        val input = code.matrix ?: throw IllegalStateException()
        val version = code.version
        val inputWidth = input.width
        val inputHeight = input.height
        var width = w
        var height = h

        val qrWidth = inputWidth + quietZone * 2
        val qrHeight = inputHeight + quietZone * 2

        if(writerType == WriterType.MINI){
            if(width < 0){
                width = inputWidth * 3 + quietZone * 2
            }
            if(height < 0){
                height = inputHeight * 3 + quietZone * 2
            }
        }else{
            if(width < 0){
                width = qrWidth
            }
            if(height < 0){
                height = qrHeight
            }
        }

        val outputWidth = Math.max(width, qrWidth)
        val outputHeight = Math.max(height, qrHeight)

        val multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight)
        // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
        // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
        // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
        // handle all the padding from 100x100 (the actual QR) up to 200x160.
        val leftPadding = (outputWidth - inputWidth * multiple) / 2
        val topPadding = (outputHeight - inputHeight * multiple) / 2

        val output = LBitMatrix(outputWidth, outputHeight)

        var outputY = topPadding

        Log.d(TAG,"version:$version,inputWidth:$inputWidth,outputWidth:$outputWidth,multiple:$multiple")

        for (inputY in 0 until  inputHeight) {
            // Write the contents of this row of the barcode
            var outputX = leftPadding
            for (inputX in 0 until  inputWidth) {

                val type = if (input.get(inputX, inputY).toInt() == 1) {
                    LBitMatrix.TYPE.BLACK
                }else{
                    LBitMatrix.TYPE.WHITE
                }

                when(writerType){

                    WriterType.DEFAULT -> {
                        output.setRegion(outputX, outputY, multiple, multiple,type)
                    }

                    WriterType.MINI -> {
                        if(isBody(version,inputWidth,inputX,inputY)){

                            output.setRegionOneInNine(outputX, outputY, multiple, multiple,type)

                        }else{

                            output.setRegion(outputX, outputY, multiple, multiple,type)

                        }
                    }

                }

                outputX += multiple
            }
            outputY += multiple
        }
        Log.d(TAG,"render end:${System.currentTimeMillis()}")
        return output
    }

    private fun isBody(version: Version,width:Int,x: Int,y: Int): Boolean{
        //左上角定位点、定位点分离层、格式化数据
        if(QRUtils.inLeftTop(x, y)){
            return false
        }
        //右上角定位点、定位点分离层、格式化数据
        if(QRUtils.inRightTop(width, x, y)){
            return false
        }
        //左下角定位点、定位点分离层、格式化数据
        if(QRUtils.inLeftBottom(width, x, y)){
            return false
        }

        //Timing Pattern基准线
        if(QRUtils.inTimingPattern(x, y)){
            return false
        }

        //辅助定位点
        if(QRUtils.isAlignmentPattern(version, width, x, y)){
            return false
        }

        return true
    }

}