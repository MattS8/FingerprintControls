package com.android.ms8.fingerprintcontrols.pages


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.content.Context
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

class AppActionsFragment : Fragment() {
    private lateinit var binding: FragmentAppActionsBinding
    private var listener: FragmentListener? = null
    private lateinit var adapter : AppInfoAdapter
    private val viewModel = AppInfoViewModel()

    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).apply {
        adapter = AppInfoAdapter()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentAppActionsBinding.inflate(inflater, container, false).apply {
            binding = this
            binding.appAdapter = adapter
            binding.applist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            listener?.bindToolbar(this.applist)

            viewModel.appList.observe(activity!!, Observer {
                adapter.setApps(it as ArrayList<AppInfo>? ?: ArrayList())
            })
        }.root

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FragmentListener")
        }
    }

    override fun onResume() = super.onResume().apply {
        binding.appAdapter = adapter
    }


    override fun onDetach() = super.onDetach()
        .also {
            Log.d("test%%", "Detatching...")
            onSaveInstanceState(Bundle())
            listener?.updateConfig()
            listener?.unbindToolbar()
            listener = null
        }


    class AppInfoViewModel : ViewModel() {
        val appList = ApkInfoFactory.AppInfoList
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppActionsFragment()
    }
}
