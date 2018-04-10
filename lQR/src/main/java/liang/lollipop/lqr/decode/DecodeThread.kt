package liang.lollipop.lqr.decode

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.ResultPointCallback
import java.util.*
import java.util.concurrent.CountDownLatch


/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 解码线程
 */
class DecodeThread(private val resultHandler: Handler, private val resultCode: ResultCode,
                   decodeFormats: Collection<BarcodeFormat>?, baseHints: Map<DecodeHintType,Any>?,
                   characterSet:String?, resultPointCallback: ResultPointCallback): Thread() {

    constructor(resultHandler: Handler, resultCode: ResultCode, decodeFormats: Collection<BarcodeFormat>?, resultPointCallback: ResultPointCallback)
            : this(resultHandler,resultCode,decodeFormats,null,null,resultPointCallback)

    constructor(resultHandler: Handler, resultCode: ResultCode, resultPointCallback: ResultPointCallback)
            : this(resultHandler,resultCode,null,resultPointCallback)

    private val handlerInitLatch = CountDownLatch(1)

    private lateinit var handler: Handler

    private val decodeUtil = DecodeUtil()

    companion object {

        const val BARCODE_BITMAP = "BARCODE_BITMAP"

        const val BARCODE_SCALED_FACTOR = "BARCODE_SCALED_FACTOR"

        const val DECODE = DecodeHandler.DECODE

        const val DECODE_BITMAP = DecodeHandler.DECODE_BITMAP

        const val QUIT = DecodeHandler.QUIT

    }

    init {

        val hints = EnumMap<DecodeHintType,Any>(DecodeHintType::class.java)

        if(baseHints != null){
            hints.putAll(baseHints)
        }
        var formats = decodeFormats
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            formats = EnumSet.noneOf(BarcodeFormat::class.java)
            formats.addAll(DecodeFormatManager.PRODUCT_FORMATS)
            formats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS)
            formats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
            formats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
            formats.addAll(DecodeFormatManager.AZTEC_FORMATS)
            formats.addAll(DecodeFormatManager.PDF417_FORMATS)
        }
        hints[DecodeHintType.POSSIBLE_FORMATS] = formats

        if (characterSet != null) {
            hints[DecodeHintType.CHARACTER_SET] = characterSet
        }

        hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] = resultPointCallback

        decodeUtil.setDecodeHints(hints)

    }

    fun getHandler(): Handler {
        try {
            handlerInitLatch.await()
        } catch (ie: InterruptedException) {
            // continue?
        }

        return handler
    }

    override fun run() {
        Looper.prepare()
        handler = DecodeHandler(decodeUtil,resultHandler,resultCode)
        handlerInitLatch.countDown()
        Looper.loop()
    }

    class ResultCode(val succeededCode: Int,val failedCode: Int)

}