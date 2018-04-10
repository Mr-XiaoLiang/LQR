package liang.lollipop.lqrdemo.bean

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

class AppInfo(var appName: String,var pkgName: String,var appIcon: Drawable,var appIntent: Intent,var isSystem: Boolean,var isHint: Boolean){

    companion object {

        fun create(packageInfo: PackageInfo,packageManager: PackageManager): AppInfo{

            val intent = packageManager.getLaunchIntentForPackage(packageInfo.packageName)

            return AppInfo(
                    packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                    packageInfo.packageName,
                    packageInfo.applicationInfo.loadIcon(packageManager),
                    intent,
                    isSystem(packageInfo),
                    isHint(packageManager,intent.component)
            )

        }

        private fun isSystem(packageInfo: PackageInfo): Boolean{
            return packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        }

        private fun isHint(packageManager: PackageManager,componentName: ComponentName): Boolean{
            return when(packageManager.getComponentEnabledSetting(componentName)){

                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> {
                    false
                }

                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> {
                    false
                }

                PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> {
                    true
                }

                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> {
                    true
                }

                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> {
                    true
                }

                else -> {
                    true
                }

            }
        }

    }

    fun getComponent(): ComponentName{
        return appIntent.component
    }

}