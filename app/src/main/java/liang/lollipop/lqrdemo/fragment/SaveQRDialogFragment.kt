package liang.lollipop.lqrdemo.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_save_qr.*
import liang.lollipop.lqrdemo.R

/**
 * 保存二维码的Dialog
 * @author Lollipop
 */
class SaveQRDialogFragment: DialogFragment(),View.OnClickListener {

    private var saveQRCallback: SaveQRCallback? = null

    private var lastWidth = 0

    companion object {
        private const val ARG_Width = "ARG_Width"

        fun newInstance(width: Int): SaveQRDialogFragment {
            val fragment = SaveQRDialogFragment()
            val args = Bundle()
            args.putInt(ARG_Width, width)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            lastWidth = arguments!!.getInt(ARG_Width)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_save_qr,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveBtn.setOnClickListener(this)
        undoBtn.setOnClickListener(this)
        widthEdit.setText("$lastWidth")
    }

    override fun onClick(v: View?) {

        when(v){

            saveBtn -> {

                val widthStr = widthEdit.text.toString()
                val width = if(widthStr.isNotEmpty()){
                    java.lang.Integer.parseInt(widthStr)
                }else{
                    -1
                }

                saveQRCallback?.okToSave(width)

                dismiss()

            }

            undoBtn -> {

                dismiss()

            }

        }

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is SaveQRCallback){
            saveQRCallback = context
        }
    }

    interface SaveQRCallback{

        fun okToSave(width: Int)

    }

}