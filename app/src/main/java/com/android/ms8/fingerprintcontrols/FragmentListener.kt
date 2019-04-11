package com.android.ms8.fingerprintcontrols.pages

import com.android.ms8.fingerprintcontrols.ConfigurationObservable

interface FragmentListener {
    fun getConfiguration() : ConfigurationObservable
}