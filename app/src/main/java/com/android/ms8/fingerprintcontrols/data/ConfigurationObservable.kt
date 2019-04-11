package com.android.ms8. fingerprintcontrols

import android.arch.lifecycle.ViewModel
import android.databinding.*
import android.widget.Switch
import com.android.ms8.fingerprintcontrols.Configuration.Companion.ACTION_BACK
import com.android.ms8.fingerprintcontrols.Configuration.Companion.ACTION_HOME
import com.android.ms8.fingerprintcontrols.Configuration.Companion.ACTION_NONE
import com.android.ms8.fingerprintcontrols.Configuration.Companion.ACTION_RECENTS
import com.android.ms8.fingerprintcontrols.Configuration.Companion.ACTION_SCROLL_LEFT
import com.android.ms8.fingerprintcontrols.Configuration.Companion.ACTION_SCROLL_RIGHT

class ConfigurationObservable(config: Configuration?) : ViewModel() {

    var swipeUpAction = ObservableInt().apply { this.set(ACTION_RECENTS) }
    var swipeDownAction = ObservableInt().apply { this.set(ACTION_HOME) }
    var swipeLeftAction = ObservableInt().apply { this.set(ACTION_BACK) }
    var swipeRightAction = ObservableInt().apply { this.set(ACTION_NONE)}

    var recentSwipeUpAction = ObservableInt().apply { this.set(ACTION_RECENTS) }
    var recentSwipeDownAction = ObservableInt().apply { this.set(ACTION_HOME) }
    var recentSwipeLeftAction = ObservableInt().apply { this.set(ACTION_SCROLL_LEFT)}
    var recentSwipeRightAction = ObservableInt().apply { this.set(ACTION_SCROLL_RIGHT) }

    var currentPage = ObservableInt().apply { this.set(R.id.navigation_main_options) }

    var bServiceEnabled = ObservableBoolean().apply { this.set(false) }
    var bRecentActionsEnabled = ObservableBoolean().apply { this.set(true) }


    init {
        swipeUpAction.set(config?.swipeUpAction ?: ACTION_RECENTS)
        swipeDownAction.set(config?.swipeDownAction ?: ACTION_HOME)
        swipeLeftAction.set(config?.swipeLeftAction ?: ACTION_BACK)
        swipeRightAction.set(config?.swipeRightAction ?: ACTION_NONE)

        recentSwipeUpAction.set(config?.recentSwipeUpAction ?: ACTION_RECENTS)
        recentSwipeDownAction.set(config?.recentSwipeDownAction ?: ACTION_HOME)
        recentSwipeLeftAction.set(config?.recentSwipeLeftAction ?: ACTION_SCROLL_LEFT)
        recentSwipeRightAction.set(config?.recentSwipeRightAction ?: ACTION_SCROLL_RIGHT)

        currentPage.set(config?.currentPage ?: R.id.navigation_main_options)

        bServiceEnabled.set(config?.bServiceEnabled ?: false)
        bRecentActionsEnabled.set(config?.bRecentActionsEnabled ?: true)
    }
}