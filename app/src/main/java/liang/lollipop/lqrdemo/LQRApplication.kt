package liang.lollipop.lqrdemo

import android.app.Application
import liang.lollipop.lbaselib.util.CrashHandler
import liang.lollipop.lqrdemo.utils.OtherUtils

/**
 * 应用上下文
 * @author Lollipop
 */
class LQRApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        CrashHandler.init(this,OtherUtils.getLogDir().absolutePath)
    }

}