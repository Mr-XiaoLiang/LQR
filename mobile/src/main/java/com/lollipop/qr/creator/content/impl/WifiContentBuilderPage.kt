package com.lollipop.qr.creator.content.impl

import com.lollipop.qr.R
import com.lollipop.qr.creator.content.ContentBuilder
import com.lollipop.qr.comm.BarcodeInfo

class WifiContentBuilderPage : ContentBuilder() {

    private var password by remember()
    private var ssid by remember()
    private var username by remember()

    override fun getContentValue(): String {
        val wifi = BarcodeInfo.Wifi()
        wifi.encryptionType = if (password.isEmpty()) {
            BarcodeInfo.Wifi.EncryptionType.OPEN
        } else {
            BarcodeInfo.Wifi.EncryptionType.WPA
        }
        wifi.ssid = ssid
        wifi.username = username
        return wifi.getBarcodeValue()
    }

    override fun buildContent(space: ItemSpace) {
        space.apply {
            Space()
            Input(
                R.string.wifi_ssid,
                InputConfig.NORMAL,
                { ssid },
            ) {
                ssid = it
            }
            Input(
                R.string.wifi_user,
                InputConfig.NORMAL,
                { username },
            ) {
                username = it
            }
            Input(
                R.string.wifi_pwd,
                InputConfig.NORMAL,
                { password },
            ) {
                password = it
            }
            SpaceEnd()
        }
    }
}