package com.android.ms8.fingerprintcontrols.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.util.Log
import com.android.ms8.fingerprintcontrols.data.AppInfo

object ApkInfoFactory {

    /**
     * Returns a list of package names of all installed apps (excluding system apps).
     */
    fun getAllInstalledApkInfo(context : Context?) : HashMap<String, AppInfo> {
        val apkInfo = HashMap<String, AppInfo>()
        val intent = Intent(Intent.ACTION_MAIN, null)
            .apply {  addCategory(Intent.CATEGORY_LAUNCHER)}
            .apply { flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) }
        val resolveInfoList = context?.packageManager?.queryIntentActivities(intent, 0)

        resolveInfoList?.forEach{
            if (!isSystemPackage(it))
                apkInfo[it.activityInfo.packageName] = AppInfo(
                    it.activityInfo.loadLabel(context.packageManager).toString(),
                    it.activityInfo.packageName,
                    Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(it.activityInfo.packageName)
                        .path(it.activityInfo.iconResource.toString())
                        .build())
        }

        return apkInfo
    }

    /**
     * Returns the app icon corresponding to the given package name or the default app icon if nothing is found
     */
    fun getAppIconByPackageName(packageName : String, context: Context) : Drawable {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e : PackageManager.NameNotFoundException) {
            Log.e("ApkInfoFactory", e.stackTrace.toString())
            ContextCompat.getDrawable(context, android.R.mipmap.sym_def_app_icon)!!
        }
    }

    /**
     * Returns the name of the app corresponding to the given package name or an empty string if nothing is found
     */
    fun getAppName(packageName: String, context: Context) : String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            if (appInfo != null)
                context.packageManager.getApplicationLabel(appInfo) as String
            else
                ""
        } catch (e : PackageManager.NameNotFoundException ) {
            Log.e("ApkInfoFactory", e.stackTrace.toString())
            ""
        }
    }

    /* ---------------- Simple helper functions ---------------- */

    private fun isSystemPackage(resolveInfo : ResolveInfo) : Boolean
            = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0


    /* Simple interface to get list from background task */
    interface AsyncResponse {
        fun appListReceived(appList: ArrayList<AppInfo>)
    }
}