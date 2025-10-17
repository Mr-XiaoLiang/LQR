package com.lollipop.qr.creator.subpage

import com.lollipop.qr.base.BaseFragment
import com.lollipop.qr.base.PigmentTheme
import com.lollipop.pigment.Pigment

open class QrBaseSubpageFragment : BaseFragment() {

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        PigmentTheme.getForePanelBackground(pigment) { bg, btn ->
            view?.setBackgroundColor(bg)
        }
    }

}