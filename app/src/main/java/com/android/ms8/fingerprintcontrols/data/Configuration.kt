package com.android.ms8.fingerprintcontrols.data

import com.android.ms8.fingerprintcontrols.R

class Configuration(observable: ConfigurationObservable?) {

    var swipeUpAction = ACTION_RECENTS
    var swipeDownAction = ACTION_HOME
    var swipeLeftAction = ACTION_BACK
    var swipeRightAction = ACTION_NONE

    var recentSwipeUpAction = RECENTS_ACTION_PREVIOUS_APP
    var recentSwipeDownAction = RECENTS_ACTION_SELECT_APP
    var recentSwipeLeftAction = RECENTS_ACTION_SCROLL_LEFT
    var recentSwipeRightAction = RECENTS_ACTION_SCROLL_RIGHT

    var currentPage = R.id.navigation_main_options

    var bServiceEnabled = false
    var bRecentActionsEnabled = true

    var bUserEnabledService = false
    set(value) { field = value && bServiceEnabled }

    init {
        swipeUpAction = observable?.swipeUpAction?.get() ?: ACTION_RECENTS
        swipeDownAction = observable?.swipeDownAction?.get() ?: ACTION_HOME
        swipeLeftAction = observable?.swipeLeftAction?.get() ?: ACTION_BACK
        swipeRightAction = observable?.swipeRightAction?.get() ?: ACTION_NONE

        recentSwipeUpAction = observable?.recentSwipeUpAction?.get() ?: RECENTS_ACTION_PREVIOUS_APP
        recentSwipeDownAction = observable?.recentSwipeDownAction?.get() ?: RECENTS_ACTION_SELECT_APP
        recentSwipeLeftAction = observable?.recentSwipeLeftAction?.get() ?: RECENTS_ACTION_SCROLL_LEFT
        recentSwipeRightAction = observable?.recentSwipeRightAction?.get() ?: RECENTS_ACTION_SCROLL_RIGHT

        currentPage = observable?.currentPage?.get() ?: R.id.navigation_main_options
        bServiceEnabled = observable?.bServiceEnabled?.get() ?: false
        bUserEnabledService = observable?.bUserEnabledService?.get() ?: false
        bRecentActionsEnabled = observable?.bRecentActionsEnabled?.get() ?: true
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