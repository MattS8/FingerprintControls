package com.android.ms8.fingerprintcontrols.pages


import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.ms8.fingerprintcontrols.adapters.AppInfoAdapter
import com.android.ms8.fingerprintcontrols.data.AppInfo
import com.android.ms8.fingerprintcontrols.databinding.FragmentAppActionsBinding
import com.android.ms8.fingerprintcontrols.listeners.FragmentListener
import com.android.ms8.fingerprintcontrols.util.ApkInfoFactory
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.ref.WeakReference

class AppActionsFragment : Fragment(), ApkInfoFactory.AsyncResponse {
    private lateinit var binding: FragmentAppActionsBinding
    private var listener: FragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentAppActionsBinding.inflate(inflater, container, false)
            .apply { binding = this }
            .apply { binding.applist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) }
            .apply { binding.appAdapter = AppInfoAdapter() }
            .apply { listener?.bindToolbar(this.applist) }
            .apply { GetAppsTask(this@AppActionsFragment, context).execute() }
            .root

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FragmentListener")
        }
    }

    override fun onDetach() = super.onDetach()
        .also {
            listener?.updateConfig()
            listener?.unbindToolbar()
            listener = null
        }

    /**
     * Listener function that adds AppInfo to adapter list
     */
    override fun appListReceived(appList: ArrayList<AppInfo>) {
        binding.appAdapter?.setApps(appList)
    }

    /**
     * Fetches saved list of modified apps and all apps on device
     */
    private class GetAppsTask(ref: ApkInfoFactory.AsyncResponse, context: Context?) : AsyncTask<Int, Nothing, ArrayList<AppInfo>>() {
        var context : WeakReference<Context?> = WeakReference(context)
        var response : WeakReference<ApkInfoFactory.AsyncResponse> = WeakReference(ref)

        override fun doInBackground(vararg params: Int?): ArrayList<AppInfo>? {
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
                return ArrayList()

            // Get hash map of all apps by name
            val allApps = context.get().let { ApkInfoFactory.getAllInstalledApkInfo(it) }

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
            allApps.forEach {allAppsList.add(it.value)}

            return allAppsList
        }

        override fun onPostExecute(result: ArrayList<AppInfo>?) {
            result?.let { response.get()?.appListReceived(it) }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppActionsFragment()
    }
}
