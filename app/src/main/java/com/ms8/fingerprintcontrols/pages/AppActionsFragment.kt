package com.ms8.fingerprintcontrols.pages


import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ms8.fingerprintcontrols.adapters.AppInfoAdapter
import com.ms8.fingerprintcontrols.databinding.FragmentAppActionsBinding
import com.ms8.fingerprintcontrols.listeners.FragmentListener
import com.ms8.fingerprintcontrols.util.ApkInfoFactory

class AppActionsFragment : Fragment() {
    private lateinit var binding: FragmentAppActionsBinding
    private var listener: FragmentListener? = null
    private lateinit var adapter : AppInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).apply {
        adapter = AppInfoAdapter()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentAppActionsBinding.inflate(inflater, container, false).apply {
            binding = this
            lifecycleOwner = this@AppActionsFragment.viewLifecycleOwner
            appAdapter = adapter
            applist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            listener?.bindToolbar(applist)

            ApkInfoFactory.AppInfoList.observe(lifecycleOwner!!, Observer {
               it?.let {adapterList ->
                   when (adapterList.posNew) {
                       // Brand new list -> simple set all data
                       -1 -> adapter.setApps(adapterList)

                       // Modifying list order
                       else -> when (adapterList.posOld) {
                           // Added to end of list
                           -1 -> adapter.notifyItemInserted(adapter.itemCount-1)

                           // Added Item removed from list and added somewhere else in the list
                           else -> {
                               adapter.notifyItemChanged(adapterList.posOld)
                               adapter.notifyItemMoved(adapterList.posOld, adapterList.posNew)
                           }
                       }
                   }
               }
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
            onSaveInstanceState(Bundle())
            listener?.updateConfig()
            listener?.unbindToolbarRecyclerView()
            listener = null
        }

    companion object {
        @JvmStatic
        fun newInstance() = AppActionsFragment()
    }
}
