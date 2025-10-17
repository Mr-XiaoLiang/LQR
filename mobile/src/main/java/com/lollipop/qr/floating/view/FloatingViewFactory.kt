package com.lollipop.qr.floating.view

interface FloatingViewFactory {

    fun create(callback: FloatingActionInvokeCallback): FloatingView

}