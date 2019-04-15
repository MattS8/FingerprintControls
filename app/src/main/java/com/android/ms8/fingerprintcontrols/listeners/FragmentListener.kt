package com.android.ms8.fingerprintcontrols

import com.android.ms8.fingerprintcontrols.data.ConfigurationObservable

interface FragmentListener {
    // Used to link fragment databinding with MainActivity
    fun getConfiguration() : ConfigurationObservable?

    // Used to tell MainActivity to apply changes to config made by fragment when it's detached
    fun updateConfig()
}