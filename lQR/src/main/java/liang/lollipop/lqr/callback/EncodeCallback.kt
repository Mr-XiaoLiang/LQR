package liang.lollipop.lqr.callback

/**
 * Created by lollipop on 2018/3/22.
 * @author Lollipop
 * 二维码编码的回调函数
 */
interface EncodeCallback<T> {

    fun onSuccess(result: T)

    fun onError(e: Exception?)

}