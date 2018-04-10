package liang.lollipop.lqrdemo.fragment

import android.os.Bundle
import android.preference.PreferenceFragment
import liang.lollipop.lqrdemo.R

/**
 * 创建二维码时的偏好设置
 * @author Lollipop
 */
class CreateQRPreferenceFragment: PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference_create_qr)
    }

}