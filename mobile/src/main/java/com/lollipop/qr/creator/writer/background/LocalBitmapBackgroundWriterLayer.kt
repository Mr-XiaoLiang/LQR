package com.lollipop.qr.creator.writer.background

import com.lollipop.qr.creator.background.BackgroundInfo
import com.lollipop.qr.creator.background.BackgroundStore


class LocalBitmapBackgroundWriterLayer : BaseBitmapBackgroundWriterLayer() {

    override fun getPhotoPathFromStore(): String {
        return BackgroundStore.getByType<BackgroundInfo.Local>()?.file?.path ?: ""
    }


}