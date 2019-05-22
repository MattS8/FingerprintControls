package com.android.ms8.fingerprintcontrols.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.FingerprintGestureController
import android.accessibilityservice.FingerprintGestureController.*
import android.accessibilityservice.GestureDescription
import android.app.Service
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.android.ms8.fingerprintcontrols.data.AppInfo
import com.android.ms8.fingerprintcontrols.data.Configuration
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_BACK
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_HOME
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_NOTIFICATIONS
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_POWER_MENU
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_QUICK_SETTINGS
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_RECENTS
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_DOWN
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_LEFT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_RIGHT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_UP
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_DEFAULT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.CONFIG
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_BACK
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_HOME
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_NONE
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_NOTIFICATIONS
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_POWER_MENU
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_QUICK_SETTINGS
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_RECENTS
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_DEFAULT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_DOWN
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_LEFT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_RIGHT
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_UP
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SELECT_APP
import com.android.ms8.fingerprintcontrols.util.ApkInfoFactory
import com.google.gson.Gson

class FingerprintService : AccessibilityService() {
    private var gestureController : FingerprintGestureController? = null
    private var fingerprintCallback : FingerprintGestureCallback? = null
    private var gestureDetectionAvailable : Boolean = false
    private var recentsShown : Boolean = false

    private lateinit var displayMetrics: DisplayMetrics
    private var middleYValue: Int = 0
    private var leftSideOfScreen: Int = 0
    private var middleXValue: Int = 0

    private var appInfo : AppInfo? = null
    //todo switch to singleton usage of configuration across app/service
    //private var configuration  = getConfig()

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event : AccessibilityEvent) {
        Log.d("DEBUG-S", "event.packageName = ${event.packageName}, event.className = ${event.className}, event.type = ${event.eventType}")
        // Check if "Recents" page is being shown
        recentsShown = bIsRecentsBeingShown(event.className)

        // Exit early if event isn't worth checking against custom apps
        if (!bIsProperActivity(event.className))
            return

        appInfo = ApkInfoFactory.AppInfoHashMap[event.packageName]
    }

    override fun onDestroy() = super.onDestroy().also {
        gestureController?.unregisterFingerprintGestureCallback(fingerprintCallback)
        self = null
    }

    override fun onCreate() = super.onCreate().also {
        // Set static variable to this running service
        self = this

        //todo check to see if service is now available, or just running
        val configJSON = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(CONFIG, Gson().toJson(Configuration(null)))
        val config = Gson().fromJson(configJSON, Configuration::class.java)
            .apply { this.bServiceEnabled = true }

        PreferenceManager.getDefaultSharedPreferences(this).edit().remove(CONFIG)
            .putString(CONFIG, Gson().toJson(config))
            .apply()
    }



    override fun onServiceConnected() {
        displayMetrics = resources.displayMetrics
        middleYValue = displayMetrics.heightPixels / 2
        leftSideOfScreen = displayMetrics.widthPixels / 4
        middleXValue = displayMetrics.widthPixels / 2
        gestureController = fingerprintGestureController
        gestureDetectionAvailable = gestureController != null && gestureController!!.isGestureDetectionAvailable

        if (fingerprintCallback != null || !gestureDetectionAvailable)
            return

        fingerprintCallback = object : FingerprintGestureCallback() {
            override fun onGestureDetectionAvailabilityChanged(available: Boolean) { gestureDetectionAvailable = available }

            override fun onGestureDetected(gesture: Int) { onSwipe(gesture) }
        }
        gestureController?.registerFingerprintGestureCallback(fingerprintCallback!!, null)
    }

    private fun onSwipe(swipeDirection : Int) {
        val config = getConfig()

        // Do nothing if service is not enabled
        if (!config.bServiceEnabled)
            return

        // Determine what action to do based on swipe and if Recent Apps page is currently shown
        when (swipeDirection) {
            FINGERPRINT_GESTURE_SWIPE_DOWN -> {
                if (config.bRecentActionsEnabled && recentsShown)
                    performRecentsAction(config.recentSwipeDownAction, swipeDirection)
                else
                    performAction(config.swipeDownAction)
            }
            FINGERPRINT_GESTURE_SWIPE_UP -> {
                if (config.bRecentActionsEnabled && recentsShown)
                    performRecentsAction(config.recentSwipeUpAction, swipeDirection)
                else
                    performAction(config.swipeUpAction)
            }
            FINGERPRINT_GESTURE_SWIPE_LEFT -> {
                if (config.bRecentActionsEnabled && recentsShown)
                    performRecentsAction(config.recentSwipeLeftAction, swipeDirection)
                else
                    performAction(config.swipeLeftAction)
            }
            FINGERPRINT_GESTURE_SWIPE_RIGHT -> {
                if (config.bRecentActionsEnabled && recentsShown)
                    performRecentsAction(config.recentSwipeRightAction, swipeDirection)
                else
                    performAction(config.swipeRightAction)
            }
        }
    }

    private fun performRecentsAction (action: Int, swipeDirection: Int)
    {
        when (action) {
            RECENTS_ACTION_NONE -> return
            RECENTS_ACTION_BACK -> performAction(ACTION_BACK)
            RECENTS_ACTION_HOME -> performAction(ACTION_HOME)
            RECENTS_ACTION_RECENTS -> performAction(ACTION_RECENTS)
            RECENTS_ACTION_NOTIFICATIONS -> performAction(ACTION_NOTIFICATIONS)
            RECENTS_ACTION_POWER_MENU -> performAction(ACTION_POWER_MENU)
            RECENTS_ACTION_QUICK_SETTINGS -> performAction(ACTION_QUICK_SETTINGS)
            RECENTS_ACTION_SCROLL_LEFT -> performAction(ACTION_SCROLL_LEFT)
            RECENTS_ACTION_SCROLL_RIGHT -> performAction(ACTION_SCROLL_RIGHT)
            RECENTS_ACTION_SCROLL_UP -> performAction(ACTION_SCROLL_UP)
            RECENTS_ACTION_SCROLL_DOWN -> performAction(ACTION_SCROLL_DOWN)
            RECENTS_ACTION_SELECT_APP -> actionTapCenter()
            RECENTS_ACTION_DEFAULT -> when(swipeDirection) {
                FINGERPRINT_GESTURE_SWIPE_DOWN -> performAction(getConfig().swipeDownAction)
                FINGERPRINT_GESTURE_SWIPE_UP -> performAction(getConfig().swipeUpAction)
                FINGERPRINT_GESTURE_SWIPE_RIGHT -> performAction(getConfig().swipeRightAction)
                FINGERPRINT_GESTURE_SWIPE_LEFT -> performAction(getConfig().swipeLeftAction)
            }
            else -> Log.e("FingerprintService", "Unknown recents action with actionId $action")
        }
    }

    private fun performAction(action: Int) {
        when (action) {
            ACTION_BACK -> performGlobalAction(GLOBAL_ACTION_BACK)
            ACTION_HOME -> performGlobalAction(GLOBAL_ACTION_HOME)
            ACTION_RECENTS -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            ACTION_NOTIFICATIONS -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            ACTION_POWER_MENU -> performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            ACTION_QUICK_SETTINGS -> performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            ACTION_SCROLL_LEFT -> actionScrollLeft()
            ACTION_SCROLL_RIGHT -> actionScrollRight()
            ACTION_SCROLL_UP -> actionScrollUp()
            ACTION_SCROLL_DOWN -> actionScrollDown()
            else -> Log.e("FingerprintService", "Unknown action with actionId $action")
        }
    }

    /* ------------------------------------------ Action Functions ------------------------------------------------- */

    private fun actionTapCenter() {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(middleXValue.toFloat(), middleYValue.toFloat())
        path.lineTo(middleXValue.toFloat(), middleYValue.toFloat())
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun actionScrollDown() {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(middleXValue.toFloat(), middleYValue.toFloat() / 2)
        path.rLineTo(0f, middleYValue.toFloat())
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun actionScrollUp() {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(middleXValue.toFloat(), (middleYValue.toFloat() * 1.75).toFloat())
        path.rLineTo(0f, -1*middleYValue.toFloat())
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun actionScrollRight() {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(100f, middleYValue.toFloat())
        path.rLineTo(middleXValue.toFloat() * 2, 0f)
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun actionScrollLeft() {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        path.moveTo((middleXValue.toFloat() * 2) - 100, middleYValue.toFloat())
        path.lineTo(100f, middleYValue.toFloat())
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun actionRemoveRecentApp() {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            path.moveTo(middleXValue.toFloat(), middleYValue.toFloat())
            path.rLineTo(0f, (-.95*middleYValue).toFloat())
        } else {
            path.moveTo(100f, middleYValue.toFloat())
            path.rLineTo(middleXValue.toFloat() * 2, 0f)
        }

        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    /* ------------------------------------------ Simple helper functions ------------------------------------------ */

    /**
     * Ensures <p>className</p> is viable for comparision against app package names.
     *
     * @param className name of class that triggered <c> AccessibilityEvent </c>
     *
     * @return <t>true</t> if className is potentially an app package name, <t>false</t> otherwise
     */
    private fun bIsProperActivity(className: CharSequence?): Boolean =
        className != null && className != "" && !className.startsWith("android.")


    /**
     * Checks class name against known packages pertaining to the "Recents" page.
     *
     * @param className name of class that triggered <c> AccessibilityEvent </c>
     *
     * @return <t>true</t> if className matches known "Recents" page package names, <t>false</t> otherwise
     */
    private fun bIsRecentsBeingShown(className: CharSequence?): Boolean =
        className == "com.android.internal.policy.impl.RecentApplicationsDialog"
                || className == "com.android.systemui.recent.RecentsActivity"
                || className == "com.android.systemui.recents.RecentsActivity"
                || className == "com.android.quickstep.RecentsActivity"

    private fun getConfig() : Configuration {
        val configJSON = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(CONFIG, Gson().toJson(Configuration(null)))
        return Gson().fromJson(configJSON, Configuration::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    companion object {
        private var self: FingerprintService? = null
        fun getServiceObject(): FingerprintService? {
            return self
        }
    }
}
