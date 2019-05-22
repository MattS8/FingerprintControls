package com.android.ms8.fingerprintcontrols.util

import android.arch.lifecycle.MutableLiveData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.android.ms8.fingerprintcontrols.data.AppInfo
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.ref.WeakReference
import java.lang.reflect.Type


object ApkInfoFactory {
    val AppInfoList : MutableLiveData<AdapterList<AppInfo>> = MutableLiveData()
    var AppInfoHashMap : HashMap<String, AppInfo> = HashMap()
    private var ModifiedApps : ArrayList<AppInfo> = ArrayList()

    /**
     * Populates the list of applications running on device and custom action information.
     */
    fun getApps(ref: AsyncResponse?, contextRef: WeakReference<Context?>) {
        GetAppsTask(ref, contextRef.get()).execute()
    }

    /**
     * Replaces the app information corresponding to the package of newAppInfo with the new information.
     * Note: This function will kick off a background task to save the revision to file storage.
     *
     * Return: TRUE if  app info was successfully replaced, FALSE if no app info with given package name
     * was found
     */
    fun replaceAppInfo(newAppInfo: AppInfo, contextRef: WeakReference<Context?>) : Boolean {
        // Remove app info from HashMap
        AppInfoHashMap.remove(newAppInfo.packageName)

        // Add new app info to HashMap
        AppInfoHashMap[newAppInfo.packageName] = newAppInfo

        // Update AppInfoList with new AppInfo
        val pos : Int = AppInfoList.value?.indexOf(newAppInfo) ?: -1

        // Update AppInfoList with new AppInfo
        val bPreviouslyCustomApp = ModifiedApps.remove(newAppInfo)
        val bHasCustomActions = newAppInfo.numberOfCustomActions > 0

        when {
            // Previously custom app and currently custom app -> stay at current pos
            (bPreviouslyCustomApp && bHasCustomActions) -> {
                AppInfoList.replace(newAppInfo, pos)

                // Add back to modified apps list
                ModifiedApps.add(newAppInfo)
            }

            // Previously custom app and no longer custom app -> move past end of custom apps
            bPreviouslyCustomApp && !bHasCustomActions -> {
                AppInfoList.replace(newAppInfo, ModifiedApps.size)
            }

            // Not previously custom app and currently custom app -> move to end of custom apps
            !bPreviouslyCustomApp && bHasCustomActions -> {
                AppInfoList.replace(newAppInfo, ModifiedApps.size)

                // Add back to modified apps list
                ModifiedApps.add(newAppInfo)
            }

            // Not previously custom app and no longer custom app -> stay at current pos
            !bPreviouslyCustomApp && !bHasCustomActions -> {
                AppInfoList.replace(newAppInfo, pos)
            }
        }

        // Convert ModifiedApps HashMap to JSON
        val appInfoJSON = GsonBuilder().registerTypeAdapter(Uri::class.java, GsonSerializers.UriSerializer()).create()
            .toJson(ModifiedApps)

        // Start background task to write new JSON to file
        WriteAppsTask(contextRef.get()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, appInfoJSON)

        return true
    }

    /* ---------------- Simple helper functions ---------------- */

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
     * Attempts to read CustomAppActions file and return said file data as a String.
     *
     * Return: file data as String or "" if read action was unsuccessful
     */
    private fun readAppInfoFile(contextRef: WeakReference<Context?>) : String {
        var fileStr = ""

        try {
            // Attempt to open file for reading
            val bufferedReader = BufferedReader(InputStreamReader(contextRef.get()?.openFileInput("CustomAppActions")))

            // Read in saved list of apps with custom actions from internal storage
            val stringBuilder = StringBuilder()
            var line: String? = bufferedReader.readLine()
            while (line != null)
                stringBuilder
                    .append(line)
                    .also { line = bufferedReader.readLine() }

            // Save to string variable for future conversion
            fileStr = stringBuilder.toString()
        } catch (e : Exception) { Log.w("ApkInfoFactory", e.message) }


        return fileStr
    }

    /**
     * Attempts to write JSON string to CustomAppActions file and returns whether
     * said process was successful.
     */
    private fun writeAppInfoFile(appInfoListJSON : String, contextRef: WeakReference<Context?>) : Boolean {
        try {
            // Attempt to open file  for writing
            val outputStreamWriter = OutputStreamWriter(contextRef.get()?.openFileOutput("CustomAppActions", Context.MODE_PRIVATE))

            // Overwrite file with new JSON object
            outputStreamWriter.write(appInfoListJSON)
            outputStreamWriter.close()
        } catch (e : Exception) { return false }

        return true
    }

    private fun isSystemPackage(resolveInfo : ResolveInfo) : Boolean
            = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

    /* ---------------- Background Tasks ---------------- */

    inline fun <reified T> parseArray(json: String, typeToken: Type): T {
        val gson = GsonBuilder().registerTypeAdapter(Uri::class.java, GsonSerializers.UriDeserializer()).create()
        return gson.fromJson<T>(json, typeToken)
    }

    /**
     * Fetches saved list of modified apps and all apps on device
     */
    private class GetAppsTask(ref: AsyncResponse?, context: Context?) : AsyncTask<Int, Nothing, Pair<AdapterList<AppInfo>, HashMap<String, AppInfo>> >() {
        var context : WeakReference<Context?> = WeakReference(context)
        var response : WeakReference<AsyncResponse?> = WeakReference(ref)

        override fun doInBackground(vararg params: Int?): Pair<AdapterList<AppInfo>, HashMap<String, AppInfo>>? {
            // Attempt to open file containing custom application actions
            val appActionsStr = readAppInfoFile(context)

            // Exit early if context reference is gone
            if (context.get() == null)
                return Pair(AdapterList(), HashMap())

            // Get hash map of all apps by name
            val allApps = getAllInstalledApkInfo(context.get())

            // Convert JSON string to temporary list (empty list if no previously modified apps)
            val type = object : TypeToken<ArrayList<AppInfo>>(){}.type
            val modifiedApps =  when (appActionsStr) {
                "" -> ArrayList()
                else -> parseArray<ArrayList<AppInfo>>(appActionsStr, type)
            }

            // Clear previous ModifiedApps
            ModifiedApps.clear()

            // Initialize list of all apps (return value)
            val allAppsList = AdapterList<AppInfo>()

            // Remove previously modified apps from all-apps list
            modifiedApps.forEach{
                if (allApps.contains(it.packageName)) {
                    // Copy iconUri from allApps list
                    it.iconUri = allApps[it.packageName]?.iconUri

                    // Remove app
                    allApps.remove(it.packageName)
                }

                // Add modified app to ModifiedApps list
                ModifiedApps.add(it)

                // Add modified app to return list
                allAppsList.add(it)
            }

            // Add the rest of the apps to all-apps list
            allAppsList.addAll(allApps.values)

            return Pair(allAppsList, allApps)
        }

        override fun onPostExecute(result: Pair<AdapterList<AppInfo>, HashMap<String, AppInfo>>?) {
            result?.let {
                AppInfoList.value = it.first
                AppInfoHashMap = it.second
                response.get()?.appListReceived(it.first)
            }
        }
    }

    /**
     * Writes new list of modified apps to file
     */
    private class WriteAppsTask(context: Context?) : AsyncTask<String, Nothing, Boolean>() {
        private val contextRef = WeakReference(context)

        override fun doInBackground(vararg params: String): Boolean? {
            writeAppInfoFile(params[0], contextRef)

            return true
        }
    }

    /* ---------------- Callbacks ---------------- */

    /* Simple interface to get list from background task */
    interface AsyncResponse {
        fun appListReceived(appList: ArrayList<AppInfo>)
    }
}