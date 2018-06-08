package com.android.matt.fingerprintcontrols

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var feedbackSheet : FeedbackBottomSheet? = null
    private var service : FingerprintService? = null

    private fun toggleControls(isChecked : Boolean) {
        if (isChecked) {
            Log.d("DEBUG", "Enable_Controls Toggle was SELECTED")
            requestPermissions(arrayOf(Manifest.permission.USE_FINGERPRINT), REQ_FINGERPRINT)
        } else {
            Log.d("DEBUG", "Enable_Controls Toggle was DESELECTED")
            setControlsChecked(false)
            disableAdvancedControlsToggle()
        }
    }

    private fun showFeedbackDialog() {
        feedbackSheet = FeedbackBottomSheet.newInstance()
        feedbackSheet!!.show(supportFragmentManager, feedbackSheet!!.tag)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        service = FingerprintService.getServiceObject()
        if (!isControlsChecked()) disableAdvancedControlsToggle()
        this.toggleControls.isChecked = isControlsChecked()
        this.toggleControlsAdvanced.isChecked = isAdvancedControlsChecked()
        this.toggleControls.setOnCheckedChangeListener { _, isChecked -> toggleControls(isChecked) }
        this.toggleControlsAdvanced.setOnCheckedChangeListener { _, isChecked -> setAdvancedControlsChecked(isChecked) }
        this.feedbackButton.setOnClickListener { showFeedbackDialog() }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQ_FINGERPRINT -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.w("DEBUG", "Permission denied")
                    Snackbar.make(this.main_frame, R.string.err_perm_denied, Snackbar.LENGTH_LONG)
                            .show()
                    return
                }

                val fpManager = baseContext.getSystemService(FingerprintManager::class.java)
                if (fpManager.isHardwareDetected) {
                    Log.d("DEBUG", "Found fingerprint sensor!")
                    setControlsChecked(true)
                    enableAdvancedControlsToggle()
                } else {
                    Log.e("DEBUG", "No fingerprint sensor hardware found or permission denied")
                    setControlsChecked(false)
                    this.toggleControls.isChecked = false
                    disableAdvancedControlsToggle()
                    Snackbar.make(this.main_frame, R.string.err_enabling, Snackbar.LENGTH_LONG)
                            .show()
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

    private fun enableAdvancedControlsToggle() {
        this.toggleControlsAdvanced.isEnabled = true
        this.tvAdvancedGestures.isEnabled = true
        this.tvAdvancedGestInfo.isEnabled = true
    }

    private fun disableAdvancedControlsToggle() {
        this.toggleControlsAdvanced.isEnabled = false
        this.tvAdvancedGestures.isEnabled = false
        this.tvAdvancedGestInfo.isEnabled = false
    }

    private fun isControlsChecked() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_CONTROLS, false)
    }

    private fun setControlsChecked(enabled : Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREF_CONTROLS, enabled).apply()
    }

    private fun isAdvancedControlsChecked() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_ADV_CONTROLS, false)
    }

    private fun setAdvancedControlsChecked(enabled : Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREF_ADV_CONTROLS, enabled).apply()
        Log.d("DEBUG", "Advanced_Controls toggle was "
                + (if (isAdvancedControlsChecked()) "SELECTED" else "DESELECTED"))
    }

    companion object {
        const val RES_FEEDBACK = 3
        const val REQ_FINGERPRINT = 8

        const val PREF_ADV_CONTROLS = "PREF_ADV_CONTROLS"
        const val PREF_CONTROLS = "PREF_CONTROLS"
    }
}
