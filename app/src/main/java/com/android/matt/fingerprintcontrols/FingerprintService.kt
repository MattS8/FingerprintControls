package com.android.matt.fingerprintcontrols

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
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_BACK
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_HOME
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_NOTIFICATIONS
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_POWER_MENU
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_QUICK_SETTINGS
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_RECENTS
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_SCROLL_DOWN
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_SCROLL_LEFT
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_SCROLL_RIGHT
import com.android.matt.fingerprintcontrols.MainActivity.Companion.ACTION_SCROLL_UP
import com.android.matt.fingerprintcontrols.MainActivity.Companion.CONFIG
import com.google.gson.Gson


class FingerprintService : AccessibilityService() {
    private var gestureController : FingerprintGestureController? = null
    private var fingerprintCallback : FingerprintGestureController.FingerprintGestureCallback? = null
    private var gestureDetectionAvailable : Boolean = false
    private var recentsShown : Boolean = false

    private lateinit var displayMetrics: DisplayMetrics
    private var middleYValue: Int = 0
    private var leftSideOfScreen: Int = 0
    private var middleXValue: Int = 0

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event : AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.className == null) {
            //Log.d("DEBUG-S", "Wrong event")
            return
        }

        val className = event.className
        Log.d("DEBUG-S", "Event = $className")
        recentsShown = (className == "com.android.internal.policy.impl.RecentApplicationsDialog"
                || className == "com.android.systemui.recent.RecentsActivity"
                || className == "com.android.systemui.recents.RecentsActivity"
                || className == "com.android.quickstep.RecentsActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DEBUG-S", "onDestroy was called!")
        gestureController?.unregisterFingerprintGestureCallback(fingerprintCallback)
        self = null
    }

    override fun onCreate() {
        super.onCreate()
        self = this
        startActivity(Intent(applicationContext, MainActivity::class.java))
        Log.d("DEBUG-S", "onCreate was called!")
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

        fingerprintCallback = object : FingerprintGestureController.FingerprintGestureCallback() {
            override fun onGestureDetectionAvailabilityChanged(available: Boolean) { gestureDetectionAvailable = available }

            override fun onGestureDetected(gesture: Int) {
                when (gesture) {
                    FINGERPRINT_GESTURE_SWIPE_DOWN -> swipeDown()
                    FINGERPRINT_GESTURE_SWIPE_UP -> swipeUp()
                    FINGERPRINT_GESTURE_SWIPE_LEFT -> swipeLeft()
                    FINGERPRINT_GESTURE_SWIPE_RIGHT -> swipeRight()
                }
            }
        }
        gestureController?.registerFingerprintGestureCallback(fingerprintCallback, null)
    }

    private fun swipeRight() {
        val config = getConfig()
        if (!config.isEnabled)
            return

        if (!recentsShown || !config.isAdvancedNavEnabled) {
            performAction(config.swipeRightAction)
            return
        }

        //Advanced Nav Code
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            actionScrollRight()
        } else {
            actionTapCenter()
        }
    }

    private fun swipeLeft() {
        val config = getConfig()
        if (!config.isEnabled)
            return
        if (!recentsShown || !config.isAdvancedNavEnabled) {
            performAction(config.swipeLeftAction)
            return
        }

        //Advanced Nav Code
        actionScrollLeft()
    }

    private fun swipeUp() {
        val config = getConfig()
        if (!config.isEnabled)
            return

        if (!recentsShown || !config.isAdvancedNavEnabled || !config.isCloseRecentAppsEnabled) {
            performAction(config.swipeUpAction)
            return
        }

        //Advanced Nav Code
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
            actionRemoveRecentApp()
        else
            actionScrollUp()
    }

    private fun swipeDown() {
        val config = getConfig()
        if (!config.isEnabled)
            return

        if (!recentsShown || !config.isAdvancedNavEnabled) {
            performAction(config.swipeDownAction)
            return
        }

        //Advanced Nav Code
        val numRecentTasks = 1
        //todo get the proper number of apps in recent apps view
        @Suppress("ConstantConditionIf")
        if (numRecentTasks <= 0) {
            performAction(GLOBAL_ACTION_BACK)
            return
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
            actionTapCenter()
        else
            actionScrollDown()
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
        }
    }

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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            val gestureBuilder = GestureDescription.Builder()
            val path = Path()
            path.moveTo(middleXValue.toFloat(), middleYValue.toFloat())
            path.rLineTo(0f, (-.95*middleYValue).toFloat())
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
            dispatchGesture(gestureBuilder.build(), null, null)
        } else {
            actionScrollRight()
        }
    }

    private fun getConfig() : Configuration {
        val configJSON = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(CONFIG, Gson().toJson(Configuration()))
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
