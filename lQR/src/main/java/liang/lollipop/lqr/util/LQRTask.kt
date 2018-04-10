package liang.lollipop.lqr.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

/**
 * Created by lollipop on 2018/3/22.
 * @author Lollipop
 */
object LQRTask {

    private val threadPool = Executors.newCachedThreadPool()

    private val sHandler: Handler = Handler(Looper.getMainLooper())

    interface CallBack<RST, in ARG> {
        fun success(result: RST)
        fun error(e: Exception?)
        fun processing(args: ARG?): RST
    }

    abstract class CallBackOnUI <RST, in ARG> private constructor(protected val handler: Handler = sHandler) : CallBack<RST, ARG> {

        override fun success(result: RST) {
            handler.post({ onUISuccess(result) })
        }

        override fun error(e: Exception?) {
            handler.post({ onUIError(e) })
        }

        override fun processing(args: ARG?): RST {
            return onBackground(args)
        }

        abstract fun onUISuccess(result: RST)
        abstract fun onUIError(e: Exception?)
        abstract fun onBackground(args: ARG?): RST

    }

    /**
     * 获取线程来执行任务
     * @param run 任务对象
     */
    fun runAs(run: Runnable) {
        threadPool.execute(run)
    }

    class Task<RST, in ARG> (private val callBack: CallBackOnUI<RST, ARG>, private val args: ARG?) : Runnable{

        override fun run() {
            try {
                val result = callBack.onBackground(args)
                callBack.success(result)
            } catch (e: Exception) {
                callBack.error(e)
            }
        }

    }

    class UITask<RST, in ARG> (private val callBack: CallBack<RST, ARG>, private val args: ARG?) : Runnable{

        override fun run() {
            try {
                val result = callBack.processing(args)
                sHandler.post({ callBack.success(result) })
            } catch (e: Exception) {
                sHandler.post({ callBack.error(e) })
            }
        }

    }

    fun <RST, ARG> addTask(callBack: CallBack<RST, ARG>, args: ARG?){
        runAs(UITask(callBack, args))
    }

    class SimpleResult<T>{
        var status = false
        var value: T? = null
    }

}