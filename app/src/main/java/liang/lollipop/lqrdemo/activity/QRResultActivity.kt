package liang.lollipop.lqrdemo.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_qrresult.*
import kotlinx.android.synthetic.main.content_qrresult.*
import liang.lollipop.lbaselib.base.BaseActivity
import liang.lollipop.lqrdemo.utils.OtherUtils
import liang.lollipop.lqrdemo.R

/**
 * 二维码返回值的Activity
 * @author Lollipop
 */
class QRResultActivity : BaseActivity() {

    companion object {

        const val ARG_IMAGE_DATA = "ARG_IMAGE_DATA"
        const val ARG_TEXT_VALUE = "ARG_TEXT_VALUE"
        const val ARG_IMAGE_ROTETION = "ARG_IMAGE_ROTETION"

        const val RESULT_EXIT = 99

    }

    private var textValue = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrresult)
        setToolbar(toolbar)
        copyBtn.setOnClickListener(this)
        exitBtn.setOnClickListener(this)
        initView()
    }

    private fun initView(){

        textValue = intent.getStringExtra(ARG_TEXT_VALUE)?:""

        messageView.text = textValue

        val byteArray = intent.getByteArrayExtra(ARG_IMAGE_DATA)
        if(byteArray != null && byteArray.isNotEmpty()){
            val bitmap = OtherUtils.Bytes2Bimap(byteArray)
            resultImageView.setImageBitmap(bitmap)
            resultImageView.rotation = intent.getIntExtra(ARG_IMAGE_ROTETION,0) * 1F
        }

    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v){

            copyBtn -> {

                OtherUtils.copy(textValue, this)
                alert().setTitle(getString(R.string.title_copy))
                        .setMessage(getString(R.string.msg_copy))
                        .setPositiveButton(R.string.btn_open_browser,{ dialog, _ ->
                            val newIntent = Intent()
                            newIntent.action = Intent.ACTION_VIEW
                            val contentUrl = Uri.parse(textValue)
                            newIntent.data = contentUrl
                            startActivity(Intent.createChooser(newIntent, getString(R.string.select_browser)))
                            dialog.dismiss()
                        })
                        .show()

            }

            exitBtn -> {

                setResult(RESULT_EXIT)
                onBackPressed()

            }

        }

    }

}
