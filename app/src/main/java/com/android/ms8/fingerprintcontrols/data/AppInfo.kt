package com.android.ms8.fingerprintcontrols.data

import android.net.Uri

class AppInfo {
    var appName = ""
    var packageName = ""
    var versionName = ""
    var iconUri : Uri? = null

    var swipeUpAction = ACTION_SAME_AS_DEFAULT
    var swipeDownAction = ACTION_SAME_AS_DEFAULT
    var swipeLeftAction = ACTION_SAME_AS_DEFAULT
    var swipeRightAction = ACTION_SAME_AS_DEFAULT

    constructor()

    constructor(name: String, pName: String, pIconUri: Uri) {
        appName = name
        packageName = pName
        iconUri = pIconUri
    }

    companion object {
        const val ACTION_NONE = 0
        const val ACTION_BACK = 1
        const val ACTION_HOME = 2
        const val ACTION_RECENTS = 3
        const val ACTION_NOTIFICATIONS = 4
        const val ACTION_POWER_MENU = 5
        const val ACTION_QUICK_SETTINGS = 6
        const val ACTION_SCROLL_LEFT = 7
        const val ACTION_SCROLL_RIGHT = 8
        const val ACTION_SCROLL_UP = 9
        const val ACTION_SCROLL_DOWN = 10
        const val ACTION_SAME_AS_DEFAULT = 11
    }
}