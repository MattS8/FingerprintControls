package com.android.matt.fingerprintcontrols

import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_BACK
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_HOME
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_NONE
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_RECENTS

class Configuration {
    var swipeUpAction = ACTION_RECENTS
    var swipeDownAction = ACTION_HOME
    var swipeLeftAction = ACTION_BACK
    var swipeRightAction = ACTION_NONE

    var isUserEnablingService = false
    var isEnabled = false
    var isAdvancedNavEnabled = true
    var isCloseRecentAppsEnabled = false
}