package com.lollipop.qr.creator.content.impl

import com.lollipop.qr.R
import com.lollipop.qr.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class SmsContentBuilderPage : ContentBuilder() {

    private var message by remember()
    private var phoneNumber by remember()

    override fun getContentValue(): String {
        val sms = BarcodeInfo.Sms()
        sms.message = message
        sms.phoneNumber = phoneNumber
        return sms.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.phone_number,
                InputConfig.PHONE,
                { phoneNumber },
            ) {
                phoneNumber = it
            }
            Space()
            Input(
                R.string.sms_message,
                InputConfig.CONTENT,
                { message },
            ) {
                message = it
            }
            SpaceEnd()
        }
    }
}