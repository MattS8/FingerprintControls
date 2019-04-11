package com.android.ms8.fingerprintcontrols

class Configuration(observable: ConfigurationObservable?) {

    var swipeUpAction =  ACTION_RECENTS
    var swipeDownAction = ACTION_HOME
    var swipeLeftAction = ACTION_BACK
    var swipeRightAction = ACTION_NONE

    var recentSwipeUpAction = ACTION_RECENTS
    var recentSwipeDownAction = ACTION_HOME
    var recentSwipeLeftAction = ACTION_SCROLL_LEFT
    var recentSwipeRightAction = ACTION_SCROLL_RIGHT

    var currentPage = R.id.navigation_main_options

    var bServiceEnabled = false
    var bRecentActionsEnabled = true

    init {
        swipeUpAction = observable?.swipeUpAction?.get() ?: ACTION_NONE
        swipeDownAction = observable?.swipeDownAction?.get() ?: ACTION_HOME
        swipeLeftAction = observable?.swipeLeftAction?.get() ?: ACTION_BACK
        swipeRightAction = observable?.swipeRightAction?.get() ?: ACTION_RECENTS

        recentSwipeUpAction = observable?.recentSwipeUpAction?.get() ?: RECENTS_ACTION_RECENTS
        recentSwipeDownAction = observable?.recentSwipeDownAction?.get() ?: RECENTS_ACTION_SELECT_APP
        recentSwipeLeftAction = observable?.recentSwipeLeftAction?.get() ?: ACTION_SCROLL_LEFT
        recentSwipeRightAction = observable?.recentSwipeRightAction?.get() ?: ACTION_SCROLL_RIGHT

        currentPage = observable?.currentPage?.get() ?: R.id.navigation_main_options
        bServiceEnabled = observable?.bServiceEnabled?.get() ?: false
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
        const val RECENTS_ACTION_RECENTS = 4
        const val RECENTS_ACTION_BACK = 5
        const val RECENTS_ACTION_HOME = 6
        const val RECENTS_ACTION_SCROLL_UP = 7
        const val RECENTS_ACTION_SCROLL_DOWN = 8
        const val RECENTS_ACTION_NOTIFICATIONS = 9
        const val RECENTS_ACTION_POWER_MENU = 10
        const val RECENTS_ACTION_QUICK_SETTINGS = 11
        const val RECENTS_ACTION_SAME_AS_DEFAULT_ACTION = 12

    }
}