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

class AppActionsFragment : Fragment(), ApkInfoFactory.AsyncResponse {
    private lateinit var binding: FragmentAppActionsBinding
    private var listener: FragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentAppActionsBinding.inflate(inflater, container, false)
            .apply { binding = this }
            .apply { binding.applist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) }
            .apply { binding.appAdapter = AppInfoAdapter() }
            .apply { addAllApps() }
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
            listener = null
        }

    /**
     * Listener function that adds AppInfo to adapter list
     */
    override fun appListReceived(applist: HashMap<String, AppInfo>?) {
        applist?.forEach {binding.appAdapter?.add(it.value)}
    }

    /**
     * Starts background tasks that fetch application info (including apps with modified actions)
     */
    private fun addAllApps() {
        try {
            // Add modified apps
            GetModifiedAppsTask(this).execute(BufferedReader(InputStreamReader(context?.openFileInput("CustomAppActions"))))
        } catch (exception : Exception) {
            Log.w("test####", exception.message)
        }

        try {
            // Add rest of apps
            GetAllAppsTask(this).execute(context)
        } catch (exception : Exception) {
            Log.w("test####", exception.message)
        }
    }

    private fun setupAdapter() {
        // Read in saved list of apps with custom actions from internal storage
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(InputStreamReader(context?.openFileInput("CustomAppAction")))
            var line: String? = bufferedReader.readLine()
            while (line != null)
                stringBuilder
                    .append(line)
                    .also { line = bufferedReader.readLine() }
        } catch (exception : Exception) {
            Log.w("test####", exception.message)
        }

        // The list of previously modified apps
        val modifiedApps = when {
            stringBuilder.isNotEmpty() -> Gson().fromJson(stringBuilder.toString(), HashMap<String, AppInfo>()::class.java)
            else -> HashMap()
        }

        // Add previously modified apps to list first (so they show up at top of list)
        val appList = ArrayList<AppInfo>(modifiedApps.values)

        // Add rest of apps to the list
        context?.let { ApkInfoFactory.GetAllInstalledApkInfo(it) }?.forEach {
            when { !modifiedApps.containsKey(it.key) -> appList.add(it.value) }
        }

        // Set binding's adapter with list of apps
        binding.appAdapter = AppInfoAdapter().apply { appList.forEach {this.add(it)} }
    }


    /* ---------------- Background Tasks ---------------- */

    private class GetModifiedAppsTask(appActionsFragment: AppActionsFragment) : AsyncTask<BufferedReader, Nothing, HashMap<String, AppInfo>>() {
        var response : ApkInfoFactory.AsyncResponse? = appActionsFragment

        override fun doInBackground(vararg params: BufferedReader?): HashMap<String, AppInfo> {
            // Read in saved list of apps with custom actions from internal storage
            val stringBuilder = StringBuilder()
            var line: String? = params[0]?.readLine()
            while (line != null)
                stringBuilder
                    .append(line)
                    .also { line = params[0]?.readLine() }

            return Gson().fromJson(stringBuilder.toString(), HashMap<String, AppInfo>()::class.java)
        }

        override fun onCancelled() = super.onCancelled().also { response = null }.also { Log.d("Test####", "CANCELED") }

        override fun onPostExecute(result: HashMap<String, AppInfo>?) {
            response?.appListReceived(result)
        }
    }

    private class GetAllAppsTask(appActionsFragment: AppActionsFragment) : AsyncTask<Context, Nothing, HashMap<String, AppInfo>>() {
        var response : ApkInfoFactory.AsyncResponse? = appActionsFragment

        override fun doInBackground(vararg params: Context?): HashMap<String, AppInfo> {
            return params[0].let { ApkInfoFactory.GetAllInstalledApkInfo(it) }
        }

        override fun onCancelled() = super.onCancelled().also { response = null }

        override fun onPostExecute(result: HashMap<String, AppInfo>?) {
            response?.appListReceived(result)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppActionsFragment()
    }
}
