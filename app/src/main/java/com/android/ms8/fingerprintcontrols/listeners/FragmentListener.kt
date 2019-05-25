package com.android.ms8.fingerprintcontrols.listeners

import android.support.v7.widget.RecyclerView
import android.widget.ScrollView
import com.android.ms8.fingerprintcontrols.data.Configuration

interface FragmentListener {
    // Used to link fragment databinding with MainActivity
    fun getConfiguration() : Configuration?

    // Used to tell MainActivity to apply changes to config made by fragment when it's detached
    fun updateConfig()

    fun bindToolbar(recyclerView: RecyclerView)

    fun bindToolbar(scrollView: ScrollView)

    fun unbindToolbarRecyclerView()

    fun unbindToolbarScrollView()
}