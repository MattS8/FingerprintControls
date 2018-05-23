package com.android.matt.fingerprintcontrols

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ServiceConnection{
    private var serviceEnabled = false
    private var feedbackSheet : FeedbackBottomSheet? = null

    override fun onServiceDisconnected(p0: ComponentName?) {
        serviceEnabled = false
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        serviceEnabled = true
        this.enabledControls.isChecked = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.enabledAdvancedControls.isEnabled = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(PREF_ADV_CONTROLS, false)
        this.enabledControls.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                this.enabledAdvancedControls.isEnabled = true
                if (!serviceEnabled) {
                    requestPermissions(arrayOf(Manifest.permission.USE_FINGERPRINT), REQ_FINGERPRINT)
                    Snackbar.make(this.main_frame, R.string.controls_enabled, Snackbar.LENGTH_SHORT)
                            .show()
                }
            } else {
                this.enabledAdvancedControls.isEnabled = false
                if (serviceEnabled) {
                    val intent = Intent(this, FingerprintService::class.java)
                    stopService(intent)
                    Snackbar.make(this.main_frame, R.string.controls_disabled, Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        }
        this.enabledAdvancedControls.setOnCheckedChangeListener {_, isChecked ->
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(PREF_ADV_CONTROLS, isChecked)
                    .apply()
        }
        this.feedbackButton.setOnClickListener {
            feedbackSheet = FeedbackBottomSheet.newInstance()
            feedbackSheet!!.show(supportFragmentManager, feedbackSheet!!.tag)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RES_FEEDBACK) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(main_frame, R.string.feedback_sent, Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(main_frame, R.string.err_feedback, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, FingerprintService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        unbindService(this)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQ_FINGERPRINT && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val fingerprintManager = baseContext.getSystemService(FingerprintManager::class.java)
            if (fingerprintManager.isHardwareDetected) {
                val intent = Intent(this, FingerprintService::class.java)
                startService(intent)
                serviceEnabled = true
            } else {
                serviceEnabled = false
                this.enabledControls.isChecked = false
                Snackbar.make(this.main_frame, R.string.err_no_fp_sensor, Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(this.main_frame, R.string.err_perm_denied, Snackbar.LENGTH_LONG).show()
        }
    }

    companion object {
        val RES_FEEDBACK = 3
        const val REQ_FINGERPRINT = 8

        const val PREF_ADV_CONTROLS = "PREF_ADV_CONTROLS"
        const val TYPE_SUGGESTION = 1
    }
}
