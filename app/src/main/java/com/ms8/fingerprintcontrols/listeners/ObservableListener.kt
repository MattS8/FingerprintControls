package com.ms8.fingerprintcontrols.listeners

interface ObservableListener {
    // Used to tell MainActivity to apply changes to config made by fragment when any observable item is changed
    fun updateConfig()
}