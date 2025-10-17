package com.lollipop.qr.other

import android.content.Intent
import android.service.quicksettings.TileService
import com.lollipop.qr.MainActivity

class ScanTileService: TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}