package com.android.matt.fingerprintcontrols

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_two.*




class MainActivity : AppCompatActivity() {
    private var feedbackSheet : FeedbackBottomSheet? = null
    private var service : FingerprintService? = null
    private lateinit var prefs : SharedPreferences
    private lateinit var  runningConfig : Configuration

    private fun toggleControls(isChecked : Boolean) {
        if (isChecked) {
            Log.d("DEBUG", "Enable_Controls Toggle was SELECTED")
            requestPermissions(arrayOf(Manifest.permission.USE_FINGERPRINT), REQ_FINGERPRINT)
        } else {
            Log.d("DEBUG", "Enable_Controls Toggle was DESELECTED")
            runningConfig.isEnabled = false
            prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig))
                    .apply()
        }
    }

    private fun showFeedbackDialog() {
        feedbackSheet = FeedbackBottomSheet.newInstance()
        feedbackSheet!!.show(supportFragmentManager, feedbackSheet!!.tag)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_two)
        service = FingerprintService.getServiceObject()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        runningConfig = Gson().fromJson(prefs.getString(CONFIG, Gson()
                .toJson(Configuration())), Configuration::class.java)
//        if (!isControlsChecked()) disableAdvancedControlsToggle()
//        this.toggleControls.isChecked = isControlsChecked()
//        this.toggleControlsAdvanced.isChecked = isAdvancedControlsChecked()
//        this.toggleControls.setOnCheckedChangeListener { _, isChecked -> toggleControls(isChecked) }
//        this.toggleControlsAdvanced.setOnCheckedChangeListener { _, isChecked -> setAdvancedControlsChecked(isChecked) }
//        this.feedbackButton.setOnClickListener { showFeedbackDialog() }
        this.feedbackButton.setOnClickListener{showFeedbackDialog()}
        this.spnSwipeLeft.setSelection(runningConfig.swipeLeftAction)
        this.spnSwipeRight.setSelection(runningConfig.swipeRightAction)
        this.spnSwipeUp.setSelection(runningConfig.swipeUpAction)
        this.spnSwipeDown.setSelection(runningConfig.swipeDownAction)
        this.spnSwipeLeft.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (pos != runningConfig.swipeLeftAction) {
                    Log.d("DEBUG", "Swipe Left: New action! (${runningConfig.swipeLeftAction} -> $pos)")
                    runningConfig.swipeLeftAction = pos
                    prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig))
                            .apply()

                } else { Log.d("DEBUG", "Swipe Left: Same action selected!") }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        this.spnSwipeRight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (pos != runningConfig.swipeRightAction) {
                    Log.d("DEBUG", "Swipe Left: New action! (${runningConfig.swipeRightAction} -> $pos)")
                    runningConfig.swipeRightAction = pos
                    prefs.edit().remove(CONFIG).putString(CONFIG,Gson().toJson(runningConfig))
                            .apply()

                } else { Log.d("DEBUG", "Swipe Left: Same action selected!") }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        this.spnSwipeUp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (pos != runningConfig.swipeUpAction) {
                    Log.d("DEBUG", "Swipe Left: New action! (${runningConfig.swipeUpAction} -> $pos)")
                    runningConfig.swipeUpAction = pos
                    prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig))
                            .apply()

                } else { Log.d("DEBUG", "Swipe Left: Same action selected!") }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        this.spnSwipeDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (pos != runningConfig.swipeDownAction) {
                    Log.d("DEBUG", "Swipe Left: New action! (${runningConfig.swipeDownAction} -> $pos)")
                    runningConfig.swipeDownAction = pos
                    prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig))
                            .apply()

                } else { Log.d("DEBUG", "Swipe Left: Same action selected!") }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        this.swToggleAdvancedNavigationControls.isChecked = runningConfig.isAdvancedNavEnabled
        this.swToggleControls.setOnCheckedChangeListener{_, isChecked -> toggleControls(isChecked)}
        this.swToggleAdvancedNavigationControls
                .setOnCheckedChangeListener{_, isChecked -> toggleAdvNav(isChecked)}
        if (service == null) {
           this.swToggleControls.isChecked = false
            //todo notify user to enable accessibility service
        } else {
            this.swToggleControls.isChecked = runningConfig.isEnabled
        }
    }

    private fun toggleAdvNav(checked: Boolean) {
        prefs
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQ_FINGERPRINT -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.w("DEBUG", "Permission denied")
                    Snackbar.make(this.main_frame, R.string.err_perm_denied,
                            Snackbar.LENGTH_LONG).show()
                    return
                }

                val fpManager = baseContext.getSystemService(FingerprintManager::class.java)
                if (fpManager.isHardwareDetected) {
                    Log.d("DEBUG", "Found fingerprint sensor!")
                    runningConfig.isEnabled = true
                    prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig))
                            .apply()
                } else {
                    Log.e("DEBUG", "No fingerprint sensor hardware found or permission denied")
                    Snackbar.make(this.main_frame, R.string.err_enabling,
                            Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RES_FEEDBACK -> {
                if (resultCode == Activity.RESULT_OK) {
                    Snackbar.make(main_frame, R.string.feedback_sent, Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(main_frame, R.string.err_feedback, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val RES_FEEDBACK = 3
        const val REQ_FINGERPRINT = 8

        const val CONFIG = "RUNNING_CONFIG"

        const val ACTION_NONE = 0
        const val ACTION_BACK = 1
        const val ACTION_HOME = 2
        const val ACTION_RECENTS = 3
        const val ACTION_NOTIFICATIONS = 4
        const val ACTION_POWER_MENU = 5
        const val ACTION_QUICK_SETTINGS = 6
    }
}
