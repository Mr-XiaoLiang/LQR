package com.lollipop.qr.creator.content.impl

import com.lollipop.qr.R
import com.lollipop.qr.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class PhoneContentBuilderPage : ContentBuilder() {

    private var number by remember()

    override fun getContentValue(): String {
        val phone = BarcodeInfo.Phone()
        phone.number = number
        return phone.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.phone_number,
                InputConfig.PHONE,
                { number },
            ) {
                number = it
            }
            SpaceEnd()
        }
    }
}