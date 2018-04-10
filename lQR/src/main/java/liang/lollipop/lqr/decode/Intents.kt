package liang.lollipop.lqr.decode

import android.content.Intent



/**
 * Created by lollipop on 2018/3/23.
 * @author Lollipop
 */
object Intents {

    /**
     * Constants related to the [Scan.ACTION] Intent.
     */
    object Scan {
        /**
         * Send this intent to open the Barcodes app in scanning mode, find a barcode, and return
         * the results.
         */
        val ACTION = "com.google.zxing.client.android.SCAN"

        /**
         * By default, sending this will decode all barcodes that we understand. However it
         * may be useful to limit scanning to certain formats. Use
         * [android.content.Intent.putExtra] with one of the values below.
         *
         * Setting this is effectively shorthand for setting explicit formats with [.FORMATS].
         * It is overridden by that setting.
         */
        val MODE = "SCAN_MODE"

        /**
         * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get
         * prices, reviews, etc. for products.
         */
        val PRODUCT_MODE = "PRODUCT_MODE"

        /**
         * Decode only 1D barcodes.
         */
        val ONE_D_MODE = "ONE_D_MODE"

        /**
         * Decode only QR codes.
         */
        val QR_CODE_MODE = "QR_CODE_MODE"

        /**
         * Decode only Data Matrix codes.
         */
        val DATA_MATRIX_MODE = "DATA_MATRIX_MODE"

        /**
         * Decode only Aztec.
         */
        val AZTEC_MODE = "AZTEC_MODE"

        /**
         * Decode only PDF417.
         */
        val PDF417_MODE = "PDF417_MODE"

        /**
         * Comma-separated list of formats to scan for. The values must match the names of
         * [com.google.zxing.BarcodeFormat]s, e.g. [com.google.zxing.BarcodeFormat.EAN_13].
         * Example: "EAN_13,EAN_8,QR_CODE". This overrides [.MODE].
         */
        val FORMATS = "SCAN_FORMATS"

        /**
         * Optional parameter to specify the id of the camera from which to recognize barcodes.
         * Overrides the default camera that would otherwise would have been selected.
         * If provided, should be an int.
         */
        val CAMERA_ID = "SCAN_CAMERA_ID"

        /**
         * @see com.google.zxing.DecodeHintType.CHARACTER_SET
         */
        val CHARACTER_SET = "CHARACTER_SET"

        /**
         * Optional parameters to specify the width and height of the scanning rectangle in pixels.
         * The app will try to honor these, but will clamp them to the size of the preview frame.
         * You should specify both or neither, and pass the size as an int.
         */
        val WIDTH = "SCAN_WIDTH"
        val HEIGHT = "SCAN_HEIGHT"

        /**
         * Desired duration in milliseconds for which to pause after a successful scan before
         * returning to the calling intent. Specified as a long, not an integer!
         * For example: 1000L, not 1000.
         */
        val RESULT_DISPLAY_DURATION_MS = "RESULT_DISPLAY_DURATION_MS"

        /**
         * Prompt to show on-screen when scanning by intent. Specified as a [String].
         */
        val PROMPT_MESSAGE = "PROMPT_MESSAGE"

        /**
         * If a barcode is found, Barcodes returns [android.app.Activity.RESULT_OK] to
         * [android.app.Activity.onActivityResult]
         * of the app which requested the scan via
         * [android.app.Activity.startActivityForResult]
         * The barcodes contents can be retrieved with
         * [android.content.Intent.getStringExtra].
         * If the user presses Back, the result code will be [android.app.Activity.RESULT_CANCELED].
         */
        val RESULT = "SCAN_RESULT"

        /**
         * Call [android.content.Intent.getStringExtra] with `RESULT_FORMAT`
         * to determine which barcode format was found.
         * See [com.google.zxing.BarcodeFormat] for possible values.
         */
        val RESULT_FORMAT = "SCAN_RESULT_FORMAT"

        /**
         * Call [android.content.Intent.getStringExtra] with `RESULT_UPC_EAN_EXTENSION`
         * to return the content of any UPC extension barcode that was also found. Only applicable
         * to [com.google.zxing.BarcodeFormat.UPC_A] and [com.google.zxing.BarcodeFormat.EAN_13]
         * formats.
         */
        val RESULT_UPC_EAN_EXTENSION = "SCAN_RESULT_UPC_EAN_EXTENSION"

        /**
         * Call [android.content.Intent.getByteArrayExtra] with `RESULT_BYTES`
         * to get a `byte[]` of raw bytes in the barcode, if available.
         */
        val RESULT_BYTES = "SCAN_RESULT_BYTES"

        /**
         * Key for the value of [com.google.zxing.ResultMetadataType.ORIENTATION], if available.
         * Call [android.content.Intent.getIntArrayExtra] with `RESULT_ORIENTATION`.
         */
        val RESULT_ORIENTATION = "SCAN_RESULT_ORIENTATION"

        /**
         * Key for the value of [com.google.zxing.ResultMetadataType.ERROR_CORRECTION_LEVEL], if available.
         * Call [android.content.Intent.getStringExtra] with `RESULT_ERROR_CORRECTION_LEVEL`.
         */
        val RESULT_ERROR_CORRECTION_LEVEL = "SCAN_RESULT_ERROR_CORRECTION_LEVEL"

        /**
         * Prefix for keys that map to the values of [com.google.zxing.ResultMetadataType.BYTE_SEGMENTS],
         * if available. The actual values will be set under a series of keys formed by adding 0, 1, 2, ...
         * to this prefix. So the first byte segment is under key "SCAN_RESULT_BYTE_SEGMENTS_0" for example.
         * Call [android.content.Intent.getByteArrayExtra] with these keys.
         */
        val RESULT_BYTE_SEGMENTS_PREFIX = "SCAN_RESULT_BYTE_SEGMENTS_"

        /**
         * Setting this to false will not save scanned codes in the history. Specified as a `boolean`.
         */
        val SAVE_HISTORY = "SAVE_HISTORY"
    }

    /**
     * Constants related to the scan history and retrieving history items.
     */
    object History {

        val ITEM_NUMBER = "ITEM_NUMBER"
    }

    /**
     * Constants related to the [Encode.ACTION] Intent.
     */
    object Encode {
        /**
         * Send this intent to encode a piece of data as a QR code and display it full screen, so
         * that another person can scan the barcode from your screen.
         */
        val ACTION = "com.google.zxing.client.android.ENCODE"

        /**
         * The data to encode. Use [android.content.Intent.putExtra] or
         * [android.content.Intent.putExtra],
         * depending on the type and format specified. Non-QR Code formats should
         * just use a String here. For QR Code, see Contents for details.
         */
        val DATA = "ENCODE_DATA"

        /**
         * The type of data being supplied if the format is QR Code. Use
         * [android.content.Intent.putExtra] with one of [Contents.Type].
         */
        val TYPE = "ENCODE_TYPE"

        /**
         * The barcode format to be displayed. If this isn't specified or is blank,
         * it defaults to QR Code. Use [android.content.Intent.putExtra], where
         * format is one of [com.google.zxing.BarcodeFormat].
         */
        val FORMAT = "ENCODE_FORMAT"

        /**
         * Normally the contents of the barcode are displayed to the user in a TextView. Setting this
         * boolean to false will hide that TextView, showing only the encode barcode.
         */
        val SHOW_CONTENTS = "ENCODE_SHOW_CONTENTS"
    }

    /**
     * Constants related to the [SearchBookContents.ACTION] Intent.
     */
    object SearchBookContents {
        /**
         * Use Google Book Search to search the contents of the book provided.
         */
        val ACTION = "com.google.zxing.client.android.SEARCH_BOOK_CONTENTS"

        /**
         * The book to search, identified by ISBN number.
         */
        val ISBN = "ISBN"

        /**
         * An optional field which is the text to search for.
         */
        val QUERY = "QUERY"
    }

    /**
     * Constants related to the [WifiConnect.ACTION] Intent.
     */
    object WifiConnect {
        /**
         * Internal intent used to trigger connection to a wi-fi network.
         */
        val ACTION = "com.google.zxing.client.android.WIFI_CONNECT"

        /**
         * The network to connect to, all the configuration provided here.
         */
        val SSID = "SSID"

        /**
         * The network to connect to, all the configuration provided here.
         */
        val TYPE = "TYPE"

        /**
         * The network to connect to, all the configuration provided here.
         */
        val PASSWORD = "PASSWORD"
    }

    /**
     * Constants related to the [Share.ACTION] Intent.
     */
    object Share {
        /**
         * Give the user a choice of items to encode as a barcode, then render it as a QR Code and
         * display onscreen for a friend to scan with their phone.
         */
        val ACTION = "com.google.zxing.client.android.SHARE"
    }

    // Not the best place for this, but, better than a new class
    // Should be FLAG_ACTIVITY_NEW_DOCUMENT in API 21+.
    // Defined once here because the current value is deprecated, so generates just one warning
    val FLAG_NEW_DOC = Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET

}