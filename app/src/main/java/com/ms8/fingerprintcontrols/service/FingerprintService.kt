package com.ms8.fingerprintcontrols.service

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
import com.ms8.fingerprintcontrols.data.AppInfo
import com.ms8.fingerprintcontrols.data.Configuration
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_BACK
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_HOME
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_NONE
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_NOTIFICATIONS
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_POWER_MENU
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_QUICK_SETTINGS
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_RECENTS
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_DOWN
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_LEFT
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_RIGHT
import com.ms8.fingerprintcontrols.data.Configuration.Companion.ACTION_SCROLL_UP
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_BACK
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_DEFAULT
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_HOME
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_NONE
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_NOTIFICATIONS
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_POWER_MENU
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_QUICK_SETTINGS
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_RECENTS
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_SCROLL_DOWN
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_SCROLL_LEFT
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_SCROLL_RIGHT
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_SCROLL_UP
import com.ms8.fingerprintcontrols.data.Configuration.Companion.CONFIG
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_BACK
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_DEFAULT
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_HOME
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_NONE
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_NOTIFICATIONS
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_POWER_MENU
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_PREVIOUS_APP
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_QUICK_SETTINGS
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_DOWN
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_LEFT
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_RIGHT
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SCROLL_UP
import com.ms8.fingerprintcontrols.data.Configuration.Companion.RECENTS_ACTION_SELECT_APP
import com.ms8.fingerprintcontrols.util.ApkInfoFactory
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

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event : AccessibilityEvent) {
        // Check if "Recents" page is being shown
        recentsShown = bIsRecentsBeingShown(event.className)

        // Exit early if event isn't worth checking against custom apps
        if (!bIsProperActivity(event.className, event.packageName))
            return

        // Set appInfo ONLY if the current app has custom actions
        appInfo = ApkInfoFactory.AppInfoHashMap[event.packageName]
        appInfo?.let {
            if (it.numberOfCustomActions < 1)
                appInfo = null
        }
    }

    override fun onDestroy() = super.onDestroy().also {
        gestureController?.unregisterFingerprintGestureCallback(fingerprintCallback)
        self = null
    }

    override fun onCreate() = super.onCreate().also {
        // Set static variable to this running service
        self = this

        val configJSON = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(CONFIG, Gson().toJson(Configuration(null)))
        val config = Gson().fromJson(configJSON, Configuration::class.java)
            .apply { this.bServiceEnabled.set(true) }

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
        gestureDetectionAvailable = gestureController?.isGestureDetectionAvailable ?: false

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
        if (!config.bServiceEnabled.get() || !config.bUserEnabledService.get())
            return


        // Perform proper action based on state of device and user-defined custom app actions
        when (val action = when {
            config.bRecentActionsEnabled.get() && recentsShown -> ActionType.RecentsAction
            appInfo != null -> ActionType.CustomAppAction
            else -> ActionType.NormalAction
        }) {
            ActionType.RecentsAction -> performRecentsAction(swipeDirection, config)
            ActionType.CustomAppAction -> performCustomAppAction(swipeDirection, config)
            ActionType.NormalAction -> performAction(swipeDirection, config)
            else -> Log.e(TAG, "Unknown action with actionId $action")
        }
    }

    /**
     * Performs a custom app action based on the swipe direction.
     */
    private fun performCustomAppAction(swipeDirection: Int, config: Configuration) {
        when (val action = when (swipeDirection) {
            FINGERPRINT_GESTURE_SWIPE_UP -> appInfo!!.swipeUpAction
            FINGERPRINT_GESTURE_SWIPE_DOWN -> appInfo!!.swipeDownAction
            FINGERPRINT_GESTURE_SWIPE_LEFT -> appInfo!!.swipeLeftAction
            FINGERPRINT_GESTURE_SWIPE_RIGHT -> appInfo!!.swipeRightAction
            else -> {
                Log.e(TAG, "Unknown swipe direction ($swipeDirection)")
                config.swipeDownAction
            }
        }) {
            APP_ACTION_NONE -> return
            APP_ACTION_BACK -> performGlobalAction(GLOBAL_ACTION_BACK)
            APP_ACTION_HOME -> performGlobalAction(GLOBAL_ACTION_HOME)
            APP_ACTION_RECENTS -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            APP_ACTION_NOTIFICATIONS -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            APP_ACTION_POWER_MENU -> performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            APP_ACTION_QUICK_SETTINGS -> performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            APP_ACTION_SCROLL_LEFT -> actionScrollLeft()
            APP_ACTION_SCROLL_RIGHT -> actionScrollRight()
            APP_ACTION_SCROLL_UP -> actionScrollUp()
            APP_ACTION_SCROLL_DOWN -> actionScrollDown()
            APP_ACTION_DEFAULT -> performAction(swipeDirection, config)
            else -> Log.e(TAG, "Unknown normal action with actionId $action")
        }
    }

    /**
     * Performs a custom action when the recents view is shown.
     */
    private fun performRecentsAction(swipeDirection: Int, config: Configuration) {

        when (val action = when (swipeDirection) {
            FINGERPRINT_GESTURE_SWIPE_UP -> config.recentSwipeUpAction.get()
            FINGERPRINT_GESTURE_SWIPE_DOWN -> config.recentSwipeDownAction.get()
            FINGERPRINT_GESTURE_SWIPE_LEFT -> config.recentSwipeLeftAction.get()
            FINGERPRINT_GESTURE_SWIPE_RIGHT -> config.recentSwipeRightAction.get()
            else -> {
                Log.e(TAG, "Unknown swipe direction ($swipeDirection)")
                config.recentSwipeDownAction.get()
            }
        }) {
            RECENTS_ACTION_NONE -> return
            RECENTS_ACTION_BACK -> performGlobalAction(GLOBAL_ACTION_BACK)
            RECENTS_ACTION_HOME -> performGlobalAction(GLOBAL_ACTION_HOME)
            RECENTS_ACTION_PREVIOUS_APP -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            RECENTS_ACTION_NOTIFICATIONS -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            RECENTS_ACTION_POWER_MENU -> performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            RECENTS_ACTION_QUICK_SETTINGS -> performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            RECENTS_ACTION_SCROLL_LEFT -> actionScrollLeft()
            RECENTS_ACTION_SCROLL_RIGHT -> actionScrollRight()
            RECENTS_ACTION_SCROLL_UP -> actionScrollUp()
            RECENTS_ACTION_SCROLL_DOWN -> actionScrollDown()
            RECENTS_ACTION_SELECT_APP -> actionTapCenter()
            RECENTS_ACTION_DEFAULT -> performAction(swipeDirection, config)
            else -> Log.e(TAG, "Unknown recents action with actionId $action")
        }
    }

    /**
     * Performs a normal action whenever recents isn't shown and current app has no custom action for
     * the given swipe direction.
     */
    private fun performAction(swipeDirection: Int, config: Configuration) {
        when (val action = when (swipeDirection) {
            FINGERPRINT_GESTURE_SWIPE_UP -> config.swipeUpAction.get()
            FINGERPRINT_GESTURE_SWIPE_DOWN -> config.swipeDownAction.get()
            FINGERPRINT_GESTURE_SWIPE_LEFT -> config.swipeLeftAction.get()
            FINGERPRINT_GESTURE_SWIPE_RIGHT -> config.swipeRightAction.get()
            else -> {
                Log.e(TAG, "Unknown swipe direction ($swipeDirection)")
                config.swipeDownAction.get()
            }
        }) {
            ACTION_NONE -> return
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
            else -> Log.e(TAG, "Unknown normal action with actionId $action")
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
     * @param className name of class that triggered <c>AccessibilityEvent</c>
     * @param packageName name of package that triggered <c>AccessibilityEvent</c>. This is checked against
     * the list of known apps in ApkFactory.ApkInfoHashMap
     *
     * @return <t>true</t> if className is potentially an app package name, <t>false</t> otherwise
     */
    private fun bIsProperActivity(className: CharSequence?, packageName: CharSequence): Boolean =
        className != null && className != "" && !className.startsWith("android.") && ApkInfoFactory.AppInfoHashMap.contains(packageName)


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
        val TAG = "FingerprintService"
        private var self: FingerprintService? = null
        fun getServiceObject(): FingerprintService? {
            return self
        }
    }

    enum class ActionType {RecentsAction, NormalAction, CustomAppAction}
}
