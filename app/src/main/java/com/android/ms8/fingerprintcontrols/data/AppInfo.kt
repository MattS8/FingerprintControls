package com.android.ms8.fingerprintcontrols.data

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.databinding.ObservableInt

class AppInfoObservable : ViewModel() {
    var appName = ObservableField<String>().apply { this.set("") }
    var packageName = ObservableField<String>().apply { this.set("") }
    var versionName = ObservableField<String>().apply { this.set("") }

    var swipeUpAction = ObservableInt().apply { this.set(ACTION_SAME_AS_DEFAULT) }
    var swipeDownAction = ObservableInt().apply { this.set(ACTION_SAME_AS_DEFAULT) }
    var swipeLeftAction = ObservableInt().apply { this.set(ACTION_SAME_AS_DEFAULT) }
    var swipeRightAction = ObservableInt().apply { this.set(ACTION_SAME_AS_DEFAULT)}

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