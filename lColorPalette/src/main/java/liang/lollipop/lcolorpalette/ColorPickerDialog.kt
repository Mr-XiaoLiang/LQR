package liang.lollipop.lcolorpalette

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import liang.lollipop.lcolorpalette.view.CirclePointView
import liang.lollipop.lcolorpalette.view.HuePaletteView
import liang.lollipop.lcolorpalette.view.SatValPaletteView


/**
 * Created by lollipop on 2018/1/23.
 * 颜色选择板的Dialog
 * @author Lollipop
 */
class ColorPickerDialog : DialogFragment(), HuePaletteView.HueCallback, SatValPaletteView.HSVCallback,
    View.OnClickListener{

    private lateinit var huePaletteView: HuePaletteView
    private lateinit var satValPaletteView: SatValPaletteView
    private lateinit var colorValueView: TextView
    private lateinit var lastColorView: CirclePointView
    private lateinit var newColorView: CirclePointView
    private lateinit var colorDoneBtn: ImageView
    private lateinit var colorArrowView: ImageView

    private var lastColor:Int = Color.TRANSPARENT

    private var selectedColor = lastColor

    private var onColorSelectedListener:OnColorSelectedListener? = null



    companion object {

        fun init(colorPrimary: Int, colorAccent: Int){
            Constants.colorAccent = colorAccent
            Constants.colorPrimary = colorPrimary
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.dialog_color_picker,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        val act = activity
        if (dialog != null && act != null) {
            val dm = DisplayMetrics()
            act.windowManager.defaultDisplay.getMetrics(dm)
            dialog.window.setLayout((dm.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    fun setColor(color: Int): ColorPickerDialog{
        lastColor = color
        selectedColor = color
        return this
    }

    fun setColorSelectedListener(listener: OnColorSelectedListener): ColorPickerDialog{
        onColorSelectedListener = listener
        return this
    }

    private fun initView(rootView: View){

        huePaletteView = rootView.findViewById(R.id.huePalette)
        satValPaletteView = rootView.findViewById(R.id.satValPalette)
        colorValueView = rootView.findViewById(R.id.colorValue)
        lastColorView = rootView.findViewById(R.id.lastColor)
        newColorView = rootView.findViewById(R.id.newColor)
        colorDoneBtn = rootView.findViewById(R.id.colorDone)
        colorArrowView = rootView.findViewById(R.id.arrowView)

        TintUtil.tintDrawable(context!!,R.drawable.ic_done_white_24dp)
                .mutate()
                .setColor(Constants.colorAccent)
                .withImageSrc(colorDoneBtn)
        TintUtil.tintDrawable(context!!,R.drawable.ic_arrow_forward_black_24dp)
                .mutate()
                .setColor(Constants.colorPrimary)
                .withImageSrc(colorArrowView)

        newColorView.setOnClickListener(this)
        colorDoneBtn.setOnClickListener(this)
        huePaletteView.setHueCallback(this)
        satValPaletteView.setHSVCallback(this)

        lastColorView.setStatusColor(lastColor)
        newColorView.setStatusColor(lastColor)

        val typeFace = Typeface.createFromAsset(context!!.assets,"fonts/number.otf")
        colorValueView.typeface = typeFace
        colorValueView.text = colorValue(lastColor)

        val hsv = FloatArray(3)
        Color.colorToHSV(lastColor,hsv)
        huePaletteView.parser(hsv[0])
        satValPaletteView.parser(hsv[1],hsv[2])
    }

    override fun onHueSelect(hue: Int) {
        satValPaletteView.onHueChange(hue.toFloat())
    }

    override fun onHSVSelect(hsv: FloatArray, rgb: Int) {
        colorValueView.text = colorValue(rgb)
        newColorView.setStatusColor(rgb)
        selectedColor = rgb
    }

    private fun colorValue(color:Int):String{
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return toHex(red)+toHex(green)+toHex(blue)
    }

    private fun toHex(value:Int):String{
        return Integer.toHexString(value)
                .let {
                    if(value < 0x10){
                        "0$it"
                    }else{
                        it
                    }
                }.toUpperCase()
    }

    override fun onClick(v: View?) {

        when(v){

            newColorView -> {
                onColorSelectedListener?.onColorSelected(this,selectedColor)
            }

        }

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is OnColorSelectedListener){
            onColorSelectedListener = context
        }
    }

    interface OnColorSelectedListener{
        fun onColorSelected(dialog: ColorPickerDialog,color:Int)
    }

}
