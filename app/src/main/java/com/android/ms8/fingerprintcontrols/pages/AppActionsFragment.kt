package com.android.ms8.fingerprintcontrols.pages


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
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

class AppActionsFragment : Fragment() {
    lateinit var binding: FragmentAppActionsBinding
    private var listener: FragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentAppActionsBinding.inflate(inflater, container, false)
            .apply { binding = this }
            .apply { setupAdapter() }
            .root

    private fun setupAdapter() {
        // Read in saved list of apps with custom actions from internal storage
        val bufferedReader = BufferedReader(InputStreamReader(context?.openFileInput("CustomAppAction")))
        val stringBuilder = StringBuilder()
        var line: String? = bufferedReader.readLine()
        while (line != null)
            stringBuilder
                .append(line)
                .also { line = bufferedReader.readLine() }
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
        binding.appAdapter = AppInfoAdapter(appList)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener?.updateConfig()
        listener = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppActionsFragment()
    }
}
