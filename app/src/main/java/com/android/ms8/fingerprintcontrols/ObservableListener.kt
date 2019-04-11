package com.android.ms8.fingerprintcontrols

interface ObservableListener {
    // Used to tell MainActivity to apply changes to config made by fragment when any observable item is changed
    fun updateConfig()
}