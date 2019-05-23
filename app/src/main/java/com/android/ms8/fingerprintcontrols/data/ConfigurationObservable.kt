package com.android.ms8.fingerprintcontrols.data

import android.arch.lifecycle.ViewModel
import android.databinding.*
import com.android.ms8.fingerprintcontrols.R
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_BACK
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_HOME
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_NONE
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_RECENTS
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_LEFT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_RIGHT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_PREVIOUS_APP
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_LEFT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_RIGHT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SELECT_APP

class ConfigurationObservable(config: Configuration?) : ViewModel() {
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
        swipeUpAction.set(config?.swipeUpAction ?: ACTION_RECENTS)
        swipeDownAction.set(config?.swipeDownAction ?: ACTION_HOME)
        swipeLeftAction.set(config?.swipeLeftAction ?: ACTION_BACK)
        swipeRightAction.set(config?.swipeRightAction ?: ACTION_NONE)

        recentSwipeUpAction.set(config?.recentSwipeUpAction ?: RECENTS_ACTION_PREVIOUS_APP)
        recentSwipeDownAction.set(config?.recentSwipeDownAction ?: RECENTS_ACTION_SELECT_APP)
        recentSwipeLeftAction.set(config?.recentSwipeLeftAction ?: RECENTS_ACTION_SCROLL_LEFT)
        recentSwipeRightAction.set(config?.recentSwipeRightAction ?: RECENTS_ACTION_SCROLL_RIGHT)

        currentPage.set(config?.currentPage ?: R.id.navigation_main_options)

        bServiceEnabled.set(config?.bServiceEnabled ?: false)
        bUserEnabledService.set(config?.bUserEnabledService ?: false)
        bRecentActionsEnabled.set(config?.bRecentActionsEnabled ?: true)
    }

    fun reset() {
        swipeUpAction.set(ACTION_RECENTS)
        swipeDownAction.set(ACTION_HOME)
        swipeLeftAction.set(ACTION_BACK)
        swipeRightAction.set(ACTION_NONE)

        recentSwipeUpAction.set(RECENTS_ACTION_PREVIOUS_APP)
        recentSwipeDownAction.set(RECENTS_ACTION_SELECT_APP)
        recentSwipeLeftAction.set(RECENTS_ACTION_SCROLL_LEFT)
        recentSwipeRightAction.set(RECENTS_ACTION_SCROLL_RIGHT)

        currentPage.set(R.id.navigation_main_options)

        bUserEnabledService.set(false)
        bRecentActionsEnabled.set(true)
    }
}