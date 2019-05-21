package com.android.ms8.fingerprintcontrols.util

import android.arch.lifecycle.MutableLiveData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.util.Log
import com.android.ms8.fingerprintcontrols.data.AppInfo
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.ref.WeakReference

object ApkInfoFactory {
    var AppInfoList : MutableLiveData<List<AppInfo>> = MutableLiveData()
    var AppInfoHashMap : HashMap<String, AppInfo> = HashMap()

    /**
     * Fetches saved list of modified apps and all apps on device
     */
    class GetAppsTask(ref: AsyncResponse?, context: Context?) : AsyncTask<Int, Nothing, Pair<ArrayList<AppInfo>, HashMap<String, AppInfo>> >() {
        var context : WeakReference<Context?> = WeakReference(context)
        var response : WeakReference<AsyncResponse?> = WeakReference(ref)

        override fun doInBackground(vararg params: Int?): Pair<ArrayList<AppInfo>, HashMap<String, AppInfo>>? {
            var appActionsStr = ""

            // Attempt to open file containing custom application actions
            try {
                val bufferedReader = BufferedReader(InputStreamReader(context.get()?.openFileInput("CustomAppActions")))

                // Read in saved list of apps with custom actions from internal storage
                val stringBuilder = StringBuilder()
                var line: String? = bufferedReader.readLine()
                while (line != null)
                    stringBuilder
                        .append(line)
                        .also { line = bufferedReader.readLine() }

                // Save to string variable for future conversion
                appActionsStr = stringBuilder.toString()
            } catch (e : Exception) { Log.w("test####", e.message) }

            // Exit early if context reference is gone
            if (context.get() == null)
                return Pair(ArrayList(), HashMap())

            // Get hash map of all apps by name
            val allApps = getAllInstalledApkInfo(context.get())

            // Convert JSON string to Hashmap (empty list if no previously modified apps)
            val modifiedApps = if (appActionsStr != "") Gson().fromJson(appActionsStr, HashMap<String, AppInfo>()::class.java)
            else HashMap()

            // Initialize list of all apps (return value)
            val allAppsList = ArrayList<AppInfo>()

            // Remove previously modified apps from all-apps list
            modifiedApps.forEach{
                if (allApps.contains(it.key)) {
                    allApps.remove(it.key)
                }

                // Add modified app to all-apps list
                allAppsList.add(it.value)
            }

            // Add the rest of the apps to all-apps list
            allAppsList.addAll(allApps.values)

            // Add the modified apps to hashmap
            allApps.putAll(modifiedApps)

            return Pair(allAppsList, allApps)
        }

        override fun onPostExecute(result: Pair<ArrayList<AppInfo>, HashMap<String, AppInfo>>?) {
            result?.let {
                response.get()?.appListReceived(it.first)
                AppInfoList.postValue(it.first)
                AppInfoHashMap = it.second
            }
        }
    }


    /**
     * Returns a list of package names of all installed apps (excluding system apps).
     */
    private fun getAllInstalledApkInfo(context : Context?) : HashMap<String, AppInfo> {
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