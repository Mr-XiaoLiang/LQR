package liang.lollipop.lqr.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch

class HandlerThread(name:String): Thread(name) {

    private val handlerInitLatch = CountDownLatch(1)

    private lateinit var handler: Handler

    private var isStart = false

    fun getHandler(): Handler {
        try {
            handlerInitLatch.await()
        } catch (ie: InterruptedException) {
        }
        return handler
    }

    fun quit(){
        handler.post { Looper.myLooper().quit() }
    }

    override fun run() {
        Looper.prepare()
        handler = Handler()
        handlerInitLatch.countDown()
        Looper.loop()
    }

}