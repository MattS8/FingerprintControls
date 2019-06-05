package com.ms8.fingerprintcontrols.adapters

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ms8.fingerprintcontrols.R
import com.ms8.fingerprintcontrols.data.AppInfo
import com.ms8.fingerprintcontrols.databinding.ItemAppInfoBinding
import com.ms8.fingerprintcontrols.util.AdapterList

//todo Add swipe-to-delete functionality
//todo Add multi-select functionality
class AppInfoAdapter : RecyclerView.Adapter<AppInfoAdapter.AppInfoHolder>() {
    private var apps : AdapterList<AppInfo> = AdapterList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemAppInfoBinding>(inflater, R.layout.item_app_info, parent, false)
        return AppInfoHolder(binding)
    }

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: AppInfoHolder, pos: Int) {
        holder.bind(apps[pos])
    }

    fun add(app : AppInfo) = apps.add(app).also { notifyItemInserted(apps.size-1) }

    fun setApps(newAppList : AdapterList<AppInfo>) {apps = newAppList.also { notifyDataSetChanged() }}

    class AppInfoHolder(dataBinding : ItemAppInfoBinding) : RecyclerView.ViewHolder(dataBinding.root) {
        var binding = dataBinding

        fun bind(appInfo: AppInfo) {
            this.binding.viewModel = appInfo
            this.binding.executePendingBindings()
        }
    }

}