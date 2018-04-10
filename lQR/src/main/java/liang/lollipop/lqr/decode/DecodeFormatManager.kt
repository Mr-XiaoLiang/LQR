package liang.lollipop.lqr.decode

import com.google.zxing.BarcodeFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap
import java.util.Arrays.asList
import android.content.Intent
import java.util.EnumSet.noneOf






/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 * 解码格式管理器
 */
object DecodeFormatManager {

    private val COMMA_PATTERN = Pattern.compile(",")

    val PRODUCT_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.RSS_14,
            BarcodeFormat.RSS_EXPANDED)
    val INDUSTRIAL_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.ITF,
            BarcodeFormat.CODABAR);
    private val ONE_D_FORMATS: Set<BarcodeFormat> = EnumSet.copyOf(PRODUCT_FORMATS).apply { addAll(INDUSTRIAL_FORMATS) }
    val QR_CODE_FORMATS = EnumSet.of(BarcodeFormat.QR_CODE)
    val DATA_MATRIX_FORMATS = EnumSet.of(BarcodeFormat.DATA_MATRIX)
    val AZTEC_FORMATS = EnumSet.of(BarcodeFormat.AZTEC)
    val PDF417_FORMATS = EnumSet.of(BarcodeFormat.PDF_417)

    private val FORMATS_FOR_MODE = HashMap<String, Set<BarcodeFormat>>().apply{

        put(Intents.Scan.ONE_D_MODE, ONE_D_FORMATS)
        put(Intents.Scan.PRODUCT_MODE, PRODUCT_FORMATS)
        put(Intents.Scan.QR_CODE_MODE, QR_CODE_FORMATS)
        put(Intents.Scan.DATA_MATRIX_MODE, DATA_MATRIX_FORMATS)
        put(Intents.Scan.AZTEC_MODE, AZTEC_FORMATS)
        put(Intents.Scan.PDF417_MODE, PDF417_FORMATS)

    }

    fun parseDecodeFormats(intent: Intent): Set<BarcodeFormat>? {
        var scanFormats: Iterable<String>? = null
        val scanFormatsString = intent.getStringExtra(Intents.Scan.FORMATS)
        if (scanFormatsString != null) {
            scanFormats = Arrays.asList(*(COMMA_PATTERN.split(scanFormatsString)))
        }
        return parseDecodeFormats(scanFormats, intent.getStringExtra(Intents.Scan.MODE))
    }

    private fun parseDecodeFormats(scanFormats: Iterable<String>?, decodeMode: String?): Set<BarcodeFormat>? {
        if (scanFormats != null) {
            val formats = EnumSet.noneOf(BarcodeFormat::class.java)
            try {
                for (format in scanFormats) {
                    formats.add(BarcodeFormat.valueOf(format))
                }
                return formats
            } catch (iae: IllegalArgumentException) {
                // ignore it then
            }

        }
        return if (decodeMode != null) {
            FORMATS_FOR_MODE[decodeMode]
        } else null
    }

}