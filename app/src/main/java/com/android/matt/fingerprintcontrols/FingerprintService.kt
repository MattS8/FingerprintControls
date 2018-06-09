package com.android.matt.fingerprintcontrols

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.FingerprintGestureController
import android.accessibilityservice.FingerprintGestureController.*
import android.accessibilityservice.GestureDescription
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.android.matt.fingerprintcontrols.MainActivity.Companion.PREF_ADV_CONTROLS
import com.android.matt.fingerprintcontrols.MainActivity.Companion.PREF_CONTROLS


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
        self = null
    }

    override fun onCreate() {
        super.onCreate()
        self = this
        Log.d("DEBUG-S", "onCreate was called!")
    }



    override fun onServiceConnected() {
        Log.d("DEBUG-S", "onServiceConnected was called!")
        //activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        Log.d("DEBUG-S", "Getting display metrics...")
        displayMetrics = resources.displayMetrics
        middleYValue = displayMetrics.heightPixels / 2
        leftSideOfScreen = displayMetrics.widthPixels / 4
        middleXValue = displayMetrics.widthPixels / 2
        Log.d("DEBUG-S", "Display Metrics: \n\tmiddleYValue = $middleYValue\n\tmiddleXValue = $middleYValue\n\tleftSideOfScreen = $leftSideOfScreen")

        Log.d("DEBUG-S", "Finding fingerprint Controller...")
        gestureController = fingerprintGestureController
        gestureDetectionAvailable = if (gestureController != null)
            gestureController!!.isGestureDetectionAvailable else false
        if (fingerprintCallback != null || !gestureDetectionAvailable)
            return

        fingerprintCallback = object : FingerprintGestureController.FingerprintGestureCallback() {
            override fun onGestureDetectionAvailabilityChanged(available: Boolean) {
                gestureDetectionAvailable = available
            }

            override fun onGestureDetected(gesture: Int) {
                when (gesture) {
                    FINGERPRINT_GESTURE_SWIPE_DOWN -> swipeDown()
                    FINGERPRINT_GESTURE_SWIPE_UP -> swipeUp()
                    FINGERPRINT_GESTURE_SWIPE_LEFT -> swipeLeft()
                    FINGERPRINT_GESTURE_SWIPE_RIGHT -> swipeRight()
                }
            }
        }
        Log.d("DEBUG-S", "Registering callback")
        gestureController?.registerFingerprintGestureCallback(fingerprintCallback, null)
    }

    private fun swipeRight() {
        if (!isEnabled())
            return
        Log.d("DEBUG-S", "swipe right detected")
        if (recentsShown && isAdvancedControlsEnabled()) {
            Log.d("DEBUG-S", "Advanced swipe right performed")
            val gestureBuilder = GestureDescription.Builder()
            val path = Path()
            path.moveTo(100f, middleYValue.toFloat())
            path.rLineTo(middleXValue.toFloat() * 2, 0f)
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
            dispatchGesture(gestureBuilder.build(), null, null)
        } else {
            // No action for Right swipe atm
        }
    }

    private fun swipeLeft() {
        if (!isEnabled()) {
            Log.d("DEBUG-S", "swipe left detected, but not currently enabled!")
            return
        }
        Log.d("DEBUG-S", "swipe left detected")
        if (recentsShown && isAdvancedControlsEnabled()) {
            Log.d("DEBUG-S", "Advanced swipe left performed")
            val gestureBuilder = GestureDescription.Builder()
            val path = Path()
            path.moveTo((middleXValue.toFloat() * 2) - 100, middleYValue.toFloat())
            path.lineTo(100f, middleYValue.toFloat())
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 200))
            dispatchGesture(gestureBuilder.build(), null, null)
        } else {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun swipeUp() {
        if (!isEnabled())
            return
        Log.d("DEBUG-S", "swipe up detected")
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    private fun swipeDown() {
        if (!isEnabled())
            return
        Log.d("DEBUG-S", "swipe down detected")
        //todo Find out if recent apps are being shown!
        if (recentsShown && isAdvancedControlsEnabled()) {
            val numRecentTasks = 1
            //todo get the proper number of apps in recent apps view
            if (numRecentTasks > 0) {
                val gestureBuilder = GestureDescription.Builder()
                val path = Path()
                path.moveTo(middleXValue.toFloat(), middleYValue.toFloat())
                path.lineTo(middleXValue.toFloat(), middleYValue.toFloat())
                gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                dispatchGesture(gestureBuilder.build(), null, null)
            } else {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        } else {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun isEnabled() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_CONTROLS, false)
    }

    private fun isAdvancedControlsEnabled() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_ADV_CONTROLS, false)
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
