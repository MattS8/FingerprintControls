package com.android.matt.fingerprintcontrols

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.FingerprintGestureController
import android.accessibilityservice.FingerprintGestureController.*
import android.accessibilityservice.GestureDescription
import android.app.Service
import android.content.Intent
import android.graphics.Path
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent


class FingerprintService : AccessibilityService() {
    private var gestureController : FingerprintGestureController? = null
    private var fingerprintCallback : FingerprintGestureController.FingerprintGestureCallback? = null
    private var gestureDetectionAvailable : Boolean = false
    private var recentsShown: Boolean = false

    private lateinit var displayMetrics: DisplayMetrics
    private var middleYValue: Int = 0
    private var leftSideOfScreen: Int = 0
    private var rightSideOfScreen: Int = 0
    private var middleXValue: Int = 0
    private var bottomSideOfScreen: Int = 0

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DEBUG-S", "onDestroy was called!")
        gestureController?.unregisterFingerprintGestureCallback(fingerprintCallback)
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        self = this
        Log.d("DEBUG-S", "onCreate was called!")
    }

    override fun onServiceConnected() {
        Log.d("DEBUG-S", "onServiceConnected was called!")
        gestureController = fingerprintGestureController
        gestureDetectionAvailable = if (gestureController != null) gestureController!!.isGestureDetectionAvailable else false

        if (fingerprintCallback != null || !gestureDetectionAvailable)
            return
        Log.d("DEBUG-S", "Setting up callback")
        displayMetrics = resources.displayMetrics
        middleYValue = displayMetrics.heightPixels / 2
        rightSideOfScreen = leftSideOfScreen * 3
        middleXValue = displayMetrics.widthPixels / 2
        bottomSideOfScreen = middleYValue * 3
        leftSideOfScreen = displayMetrics.widthPixels / 4

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

        if (fingerprintCallback != null)
            gestureController?.registerFingerprintGestureCallback(fingerprintCallback, null)
    }

    private fun swipeRight() {
        Log.d("DEBUG-S", "swipe right detected")
        if (recentsShown && advancedControlsEnabled()) {
            val gestureBuilder = GestureDescription.Builder()
            val path = Path()
            path.moveTo(leftSideOfScreen.toFloat(), middleYValue.toFloat())
            path.lineTo(rightSideOfScreen.toFloat(), middleYValue.toFloat())
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 100, 50))
            dispatchGesture(gestureBuilder.build(), null, null)
        } else {
            // No action for Right swipe atm
        }
    }

    private fun swipeLeft() {
        Log.d("DEBUG-S", "swipe left detected")
        if (recentsShown && advancedControlsEnabled()) {
            val gestureBuilder = GestureDescription.Builder()
            val path = Path()
            path.moveTo(rightSideOfScreen.toFloat(), middleYValue.toFloat())
            path.lineTo(leftSideOfScreen.toFloat(), middleYValue.toFloat())
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 100, 50))
            dispatchGesture(gestureBuilder.build(), null, null)
        } else {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun swipeUp() {
        Log.d("DEBUG-S", "swipe up detected")
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    private fun swipeDown() {
        Log.d("DEBUG-S", "swipe down detected")
        //todo Find out if recent apps are being shown!
        if (recentsShown && advancedControlsEnabled()) {
            val gestureBuilder = GestureDescription.Builder()
            val path = Path()
            path.moveTo(middleXValue.toFloat(), middleYValue.toFloat())
            path.lineTo(middleXValue.toFloat(), bottomSideOfScreen.toFloat())
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 100, 50))
            dispatchGesture(gestureBuilder.build(), null, null)
        } else {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun advancedControlsEnabled(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(MainActivity.PREF_ADV_CONTROLS, false)
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
