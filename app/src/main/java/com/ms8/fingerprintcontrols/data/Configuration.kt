package com.ms8.fingerprintcontrols.data

import android.arch.lifecycle.ViewModel
import android.databinding.*
import com.ms8.fingerprintcontrols.R

class Configuration(config: Configuration? = null) : ViewModel() {
    var swipeUpAction = ObservableInt().apply { this.set(ACTION_RECENTS) }
    var swipeDownAction = ObservableInt().apply { this.set(ACTION_HOME) }
    var swipeLeftAction = ObservableInt().apply { this.set(ACTION_BACK) }
    var swipeRightAction = ObservableInt().apply { this.set(ACTION_NONE)}

    var recentSwipeUpAction = ObservableInt().apply { this.set(RECENTS_ACTION_PREVIOUS_APP) }
    var recentSwipeDownAction = ObservableInt().apply { this.set(RECENTS_ACTION_SELECT_APP) }
    var recentSwipeLeftAction = ObservableInt().apply { this.set(RECENTS_ACTION_SCROLL_LEFT)}
    var recentSwipeRightAction = ObservableInt().apply { this.set(RECENTS_ACTION_SCROLL_RIGHT) }

    var currentPage = ObservableInt().apply { this.set(R.id.navigation_main_options) }

    var bServiceEnabled = ObservableBoolean().apply { this.set(false) }
    var bUserEnabledService = ObservableBoolean().apply { this.set(false) }
    var bRecentActionsEnabled = ObservableBoolean().apply { this.set(true) }

    init {
        config?.let {
            setup(it)
        }
    }

    private fun setup(config: Configuration) {
        swipeUpAction.set(config.swipeUpAction.get())
        swipeDownAction.set(config.swipeDownAction.get())
        swipeLeftAction.set(config.swipeLeftAction.get())
        swipeRightAction.set(config.swipeRightAction.get())

        recentSwipeUpAction.set(config.recentSwipeUpAction.get())
        recentSwipeDownAction.set(config.recentSwipeDownAction.get())
        recentSwipeLeftAction.set(config.recentSwipeLeftAction.get())
        recentSwipeRightAction.set(config.recentSwipeRightAction.get())

        currentPage.set(config.currentPage.get())

        bServiceEnabled.set(config.bServiceEnabled.get())
        bUserEnabledService.set(config.bUserEnabledService.get())
        bRecentActionsEnabled.set(config.bRecentActionsEnabled.get())
    }

    fun reset() {
        setup(Configuration())
    }

    companion object {
        const val CONFIG = "FPC_CONFIG"

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

        const val RECENTS_ACTION_NONE = 0
        const val RECENTS_ACTION_SCROLL_LEFT = 1
        const val RECENTS_ACTION_SCROLL_RIGHT = 2
        const val RECENTS_ACTION_SELECT_APP = 3
        const val RECENTS_ACTION_PREVIOUS_APP = 4
        const val RECENTS_ACTION_BACK = 5
        const val RECENTS_ACTION_HOME = 6
        const val RECENTS_ACTION_SCROLL_UP = 7
        const val RECENTS_ACTION_SCROLL_DOWN = 8
        const val RECENTS_ACTION_NOTIFICATIONS = 9
        const val RECENTS_ACTION_POWER_MENU = 10
        const val RECENTS_ACTION_QUICK_SETTINGS = 11
        const val RECENTS_ACTION_DEFAULT = 12

        const val APP_ACTION_NONE = 0
        const val APP_ACTION_BACK = 1
        const val APP_ACTION_HOME = 2
        const val APP_ACTION_RECENTS = 3
        const val APP_ACTION_NOTIFICATIONS = 4
        const val APP_ACTION_POWER_MENU = 5
        const val APP_ACTION_QUICK_SETTINGS = 6
        const val APP_ACTION_SCROLL_LEFT = 7
        const val APP_ACTION_SCROLL_RIGHT = 8
        const val APP_ACTION_SCROLL_UP = 9
        const val APP_ACTION_SCROLL_DOWN = 10
        const val APP_ACTION_DEFAULT = 11
    }
}