package com.android.ms8.fingerprintcontrols

import android.Manifest
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableInt
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Switch
import com.android.ms8.fingerprintcontrols.data.Configuration
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.CONFIG
import com.android.ms8.fingerprintcontrols.data.ConfigurationObservable
import com.android.ms8.fingerprintcontrols.databinding.MainActivityBinding
import com.android.ms8.fingerprintcontrols.pages.AppActionsFragment
import com.android.ms8.fingerprintcontrols.pages.HelpFragment
import com.android.ms8.fingerprintcontrols.pages.MainOptionsFragment
import com.android.ms8.fingerprintcontrols.service.FingerprintService
import com.google.gson.Gson
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity(), FragmentListener, ObservableListener {
    lateinit var binding : MainActivityBinding
    lateinit var config: ConfigurationObservable
    /**
     * Listener for bottom nav bar that loads the proper fragment.
     */
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var fragment : Fragment? = null

        // Get fragment based on item selected
        when (item.itemId) {
            R.id.navigation_main_options -> { fragment = MainOptionsFragment.newInstance() }
            R.id.navigation_app_actions -> { fragment = AppActionsFragment.newInstance() }
            R.id.navigation_about -> { fragment = HelpFragment.newInstance() }
        }

        // Update config file with current page selection
        binding.configuration?.currentPage?.set(item.itemId)
        updateConfig()

        // Set title based on selected page
        titlePage.text = getPageTitle(binding.configuration?.currentPage?.get())

        // Load fragment and return success/failure
        loadFragment(fragment)
    }

    /**
     * Checks for fingerprint permission. If the permission is denied, a Snackbar show a notification to the user
     * explaining that the app will not work without the permission. If the permission is accepted, check whether
     * fingerprint hardware is found and report results accordingly.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQ_FINGERPRINT -> {
                    when {
                    // Permission was denied
                    grantResults[0] != PackageManager.PERMISSION_GRANTED -> {
                        // Update configuration file
                        binding.configuration?.bServiceEnabled?.set(false)
                        updateConfig()
                        // Show notification to user
                        Snackbar.make(this.container, R.string.err_perm_denied, Snackbar.LENGTH_LONG).show()
                    }

                    // Permission was accepted and fingerprint hardware was found

                       FingerprintManagerCompat.from(this).isHardwareDetected -> {
                        // Update configuration file
                        binding.configuration?.bServiceEnabled?.set(true)
                        updateConfig()

                        // Start service
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivityForResult(intent, RES_ACCESSIBILITY)
                    }

                    // Permission was accepted but no fingerprint hardware was found
                    else -> {
                        // Update configuration file
                        binding.configuration?.bServiceEnabled?.set(false)
                        updateConfig()
                        // Show notification to user
                        Snackbar.make(this.container, R.string.err_enabling, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.report) {
                                //todo Add reporting feature
                                Snackbar.make(this.container, R.string.thanks_for_reporting, Snackbar.LENGTH_SHORT).show()
                            }.show()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        config.bServiceEnabled.set(FingerprintService.getServiceObject() != null)
        Log.d("test###", "OnResume called! (bServiceEnabled = ${config.bServiceEnabled.get()})")
    }

    override fun onPause() {
        super.onPause()
        Log.d("test###", "OnPause called!")
        updateConfig()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.lifecycleOwner = this

        // Get saved/default configuration file
        config = ConfigurationObservable(
            Gson()
                .fromJson(
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(CONFIG, Gson().toJson(Configuration(null))),
                    Configuration::class.java
                )
        )
        binding.configuration = config

        // Get copy of saved/default configuration file
        val config = binding.configuration

        // Set bottom nav bar listener
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Load last viewed page
        when (config?.currentPage?.get()) {
            // General Actions Page
            R.id.navigation_main_options -> loadFragment(MainOptionsFragment.newInstance())
            // App Actions Page
            R.id.navigation_app_actions -> loadFragment(AppActionsFragment.newInstance())
            // About Page
            R.id.navigation_about -> loadFragment(HelpFragment.newInstance())
        }

        // Set bottom nav bar to last viewed page
        navigation.selectedItemId = config?.currentPage?.get() ?: R.id.navigation_main_options

        // Set title based on selected page
        titlePage.text = getPageTitle(config?.currentPage?.get())

        // Add callbacks for spinners to detect changes instantly
        addSpinnerCallbacks()
    }

    /* ------------------------------------------ Simple helper functions ------------------------------------------ */

    /** Adds callback to all spinners that updates config immediately after change **/
    private fun addSpinnerCallbacks() {
        config.swipeDownAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateConfig()
            }
        })
        config.swipeUpAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateConfig()
            }
        })
        config.swipeLeftAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateConfig()
            }
        })
        config.swipeRightAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateConfig()
            }
        })

        config.recentSwipeUpAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateConfig()
            }
        })
        config.recentSwipeDownAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateConfig()
            }
        })
        config.recentSwipeLeftAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateConfig()
            }
        })
        config.recentSwipeRightAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateConfig()
            }
        })
    }

    /** Loads a fragment into the frag_container when the fragment parameter is not null. **/
    private fun loadFragment(fragment: Fragment?) : Boolean {
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frag_container, fragment)
                .commit()
            return true
        }

        return false
    }

    /** Returns the title of the page corresponding to the page int resource **/
    private fun getPageTitle(page: Int?): CharSequence? {
        return when (page)
        {
            R.id.navigation_main_options -> getString(R.string.title_main_options)
            R.id.navigation_app_actions -> getString(R.string.title_app_actions)
            R.id.navigation_about -> getString(R.string.title_help)
            else -> ""
        }
    }

    /** Updates preferences with latest config **/
    override fun updateConfig() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .remove(CONFIG)
            .putString(CONFIG, Gson().toJson(Configuration(binding.configuration)))
            .apply()
    }

    /** A simple getter for the current copy of the configuration file **/
    override fun getConfiguration(): ConfigurationObservable? {
        return config
    }

    companion object {
        const val REQ_FINGERPRINT = 88
        const val RES_ACCESSIBILITY = 5
    }
}
