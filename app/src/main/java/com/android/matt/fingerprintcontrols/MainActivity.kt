package com.android.matt.fingerprintcontrols

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
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

        this.feedbackButton.setOnClickListener{showFeedbackDialog()}
        this.spnSwipeLeft.setSelection(runningConfig.swipeLeftAction)
        this.spnSwipeRight.setSelection(runningConfig.swipeRightAction)
        this.spnSwipeUp.setSelection(runningConfig.swipeUpAction)
        this.spnSwipeDown.setSelection(runningConfig.swipeDownAction)
        this.spnSwipeLeft.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos != runningConfig.swipeLeftAction) {
                    runningConfig.swipeLeftAction = pos
                    prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig))
                            .apply()

                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        this.spnSwipeRight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos != runningConfig.swipeRightAction) {
                    runningConfig.swipeRightAction = pos
                    prefs.edit().remove(CONFIG).putString(CONFIG,Gson().toJson(runningConfig))
                            .apply()

                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        this.spnSwipeUp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos != runningConfig.swipeUpAction) {
                    runningConfig.swipeUpAction = pos
                    prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig))
                            .apply()

                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        this.spnSwipeDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos != runningConfig.swipeDownAction) {
                    runningConfig.swipeDownAction = pos
                    prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig))
                            .apply()

                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
        this.swToggleAdvNavControls.isChecked = runningConfig.isAdvancedNavEnabled
        this.swToggleControls.isChecked = service != null && runningConfig.isEnabled
        this.swCloseAppRecents.isChecked = runningConfig.isCloseRecentAppsEnabled
        this.swCloseAppRecents.isEnabled = runningConfig.isAdvancedNavEnabled
        this.tvCloseAppRecentsDesc.isEnabled = runningConfig.isAdvancedNavEnabled
        this.swToggleControls.setOnCheckedChangeListener{_, isChecked -> toggleControls(isChecked)}
        this.swToggleAdvNavControls.setOnCheckedChangeListener{_, isChecked -> toggleAdvNav(isChecked)}
        this.swCloseAppRecents.setOnCheckedChangeListener{_, isChecked -> toggleCloseAppRecents(isChecked)}
    }

    override fun onResume() {
        super.onResume()
        service = FingerprintService.getServiceObject()
        if (service == null) {
            AlertDialog.Builder(this, android.R.style.ThemeOverlay_Material_Dialog_Alert)
                    .setTitle(getString(R.string.alert_title))
                    .setMessage(getString(R.string.access_desc_short))
                    .setPositiveButton(getString(R.string.enable), { d, _ -> enable(d) })
                    .setNegativeButton(getString(R.string.what_this), { dialog, _ -> explain(dialog)})
                    .setIcon(R.drawable.ic_warning_24dp)
                    .show()
        }
    }

    private fun explain(dialog: DialogInterface?) {
        dialog?.dismiss()
        AlertDialog.Builder(this, android.R.style.ThemeOverlay_Material_Dialog_Alert)
                .setTitle(getString(R.string.about_accessibility_title))
                .setMessage(getString(R.string.accessibility_explination))
                .setPositiveButton(getString(R.string.enable_service), {d,_ -> enable(d)})
                .setNegativeButton(getString(R.string.no_thanks), {d,_ -> quit(d)})
                .show()
    }

    private fun quit(dialog: DialogInterface) {
        dialog.dismiss()
    }

    private fun enable(dialog: DialogInterface?) {
        dialog?.dismiss()
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, RES_ACCESSIBILITY)
    }

    private fun toggleAdvNav(checked: Boolean) {
        if (!checked) {
            swCloseAppRecents.isChecked = false
            runningConfig.isCloseRecentAppsEnabled = false
        }
        swCloseAppRecents.isEnabled = checked
        tvCloseAppRecentsDesc.isEnabled = checked

        runningConfig.isAdvancedNavEnabled = checked
        prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig)).apply()
    }

    private fun toggleCloseAppRecents(checked: Boolean) {
        runningConfig.isCloseRecentAppsEnabled = checked
        prefs.edit().remove(CONFIG).putString(CONFIG, Gson().toJson(runningConfig)).apply()
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
                feedbackSheet?.dismiss()
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
        const val RES_ACCESSIBILITY = 5
        const val REQ_FINGERPRINT = 8

        const val CONFIG = "RUNNING_CONFIG"

        const val ACTION_NONE = 0
        const val ACTION_BACK = 1
        const val ACTION_HOME = 2
        const val ACTION_RECENTS = 3
        const val ACTION_NOTIFICATIONS = 4
        const val ACTION_POWER_MENU = 5
        const val ACTION_QUICK_SETTINGS = 6
        const val ACTION_SCROLL_LEFT = 7
        const val ACTION_SCROLL_RIGHT = 8
        const val ACTION_SCROLL_UP = 9
        const val ACTION_SCROLL_DOWN = 10
    }
}
