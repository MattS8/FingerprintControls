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
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ServiceConnection{
    private var serviceEnabled = false
    private var feedbackSheet : FeedbackBottomSheet? = null
    private var service : FingerprintService? = null

    override fun onServiceDisconnected(p0: ComponentName?) {
        serviceEnabled = false
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        serviceEnabled = true
        this.toggleControls.isChecked = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        service = FingerprintService.getServiceObject()
        this.toggleControls.isChecked = isControlsEnabled()
        this.toggleControlsAdvanced.isEnabled = isAdvancedControlsEnabled()
        this.toggleControls.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d("DEBUG", "Enable_Controls Toggle was SELECTED")
                requestPermissions(arrayOf(Manifest.permission.USE_FINGERPRINT), REQ_FINGERPRINT)
                Snackbar.make(this.main_frame, R.string.controls_enabled, Snackbar.LENGTH_SHORT)
                        .show()
            } else {
                Log.d("DEBUG", "Enable_Controls Toggle was DESELECTED")
                this.toggleControlsAdvanced.isEnabled = false
                setControlsEnabled(false)
                Log.d("DEBUG", "Service is being stopped")
                stopService(Intent(this, FingerprintService::class.java))
                unbindService(this)
                if (service != null) {
                    Log.d("DEBUG", "manually calling onDestroy")
                    service!!.onDestroy()
                }

                Snackbar.make(this.main_frame, R.string.controls_disabled, Snackbar.LENGTH_SHORT)
                        .show()
            }
        }
        this.toggleControlsAdvanced.setOnCheckedChangeListener { _, isChecked ->
            setAdvancedControlsEnabled(isChecked)
            Log.d("DEBUG", "Advanced_Controls toggle was "
                    + (if (isAdvancedControlsEnabled()) "SELECTED" else "DESELECTED"))
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
        if (isControlsEnabled()) {
            Log.d("DEBUG", "OnResume: Service enabled")
            val intent = Intent(this, FingerprintService::class.java)
            bindService(intent, this, Context.BIND_AUTO_CREATE)
            service = FingerprintService.getServiceObject()
        } else {
            Log.d("DEBUG", "OnResume: Service disabled")
        }
    }

    override fun onPause() {
        super.onPause()
        if (isControlsEnabled()) {
            Log.d("DEBUG", "OnPause: Unbinding service")
            unbindService(this)
        } else {
            Log.d("DEBUG", "OnPause: No service to unbind")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQ_FINGERPRINT -> {
                val fpManager = baseContext.getSystemService(FingerprintManager::class.java)
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && fpManager.isHardwareDetected) {
                    Log.d("DEBUG", "Found fingerprint sensor!")
                    val intent = Intent(this, FingerprintService::class.java)
                    startService(intent)
                    bindService(intent, this, Context.BIND_AUTO_CREATE)
                    service = FingerprintService.getServiceObject()
                    setControlsEnabled(true)
                    this.toggleControlsAdvanced.isEnabled = true
                } else {
                    Log.e("DEBUG", "No fingerprint sensor hardware found")
                    setControlsEnabled(false)
                    this.toggleControls.isChecked = false
                    this.toggleControlsAdvanced.isEnabled = false
                    Snackbar.make(this.main_frame, R.string.err_enabling, Snackbar.LENGTH_LONG)
                            .show()
                }
            }
        }
    }

    private fun isControlsEnabled() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_CONTROLS, false)
    }

    private fun setControlsEnabled(enabled : Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREF_CONTROLS, enabled).apply()
    }

    private fun isAdvancedControlsEnabled() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_ADV_CONTROLS, false)
    }

    private fun setAdvancedControlsEnabled(enabled : Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREF_ADV_CONTROLS, enabled).apply()
    }

    companion object {
        const val RES_FEEDBACK = 3
        const val REQ_FINGERPRINT = 8

        const val PREF_ADV_CONTROLS = "PREF_ADV_CONTROLS"
        const val PREF_CONTROLS = "PREF_CONTROLS"
    }
}
