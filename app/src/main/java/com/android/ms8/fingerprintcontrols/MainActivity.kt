package com.android.ms8.fingerprintcontrols

import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.widget.ScrollView
import com.android.ms8.fingerprintcontrols.data.Configuration
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.CONFIG
import com.android.ms8.fingerprintcontrols.data.ConfigurationObservable
import com.android.ms8.fingerprintcontrols.databinding.MainActivityBinding
import com.android.ms8.fingerprintcontrols.listeners.FragmentListener
import com.android.ms8.fingerprintcontrols.listeners.ObservableListener
import com.android.ms8.fingerprintcontrols.pages.AppActionsFragment
import com.android.ms8.fingerprintcontrols.pages.HelpFragment
import com.android.ms8.fingerprintcontrols.pages.MainOptionsFragment
import com.android.ms8.fingerprintcontrols.service.FingerprintService
import com.android.ms8.fingerprintcontrols.util.ApkInfoFactory
import com.google.gson.Gson
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity(), FragmentListener, ObservableListener {
    lateinit var binding : MainActivityBinding
    lateinit var config: ConfigurationObservable
    /**
     * Listener for bottom nav bar that loads the proper fragment.
     */
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        // Set current selected page
        binding.configuration?.currentPage?.set(item.itemId)

        // Set title based on selected page
        toolbar.title = getPageTitle(item.itemId)
        // Load fragment and return success/failure
        loadFragment(item.itemId)

        return@OnNavigationItemSelectedListener true
    }

    /**
     * Checks for fingerprint permission. If the permission is denied, a Snackbar show a notification to the user
     * explaining that the app will not work without the permission. If the permission is accepted, check whether
     * fingerprint hardware is found and report results accordingly.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
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

                        // Start service if no service is running
                        when {
                            FingerprintService.getServiceObject() == null ->
                                startActivityForResult(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), RES_ACCESSIBILITY)
                            else -> {}
                        }
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
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    override fun onResume() = super.onResume()
        .also { config.bServiceEnabled.set(FingerprintService.getServiceObject() != null) }

    override fun onPause() = super.onPause()
        .also { updateConfig() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.lifecycleOwner = this

        // Start background task to fetch AppInfo
        ApkInfoFactory.GetAppsTask(null, this).execute()

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
        config?.currentPage?.get()?.let { loadFragment(it) }

        // Set bottom nav bar to last viewed page
        navigation.selectedItemId = config?.currentPage?.get() ?: R.id.navigation_main_options

        // todo Link toolbar with scroll view

        // Set title based on selected page
        toolbar.title = getPageTitle(config?.currentPage?.get())

        // Add callbacks for spinners to detect changes instantly
        addSpinnerCallbacks()
    }

    /* ------------------------------------------ Simple helper functions ------------------------------------------ */

    /** Adds callback to all spinners that updates config immediately after change **/
    private fun addSpinnerCallbacks() {
        config.swipeDownAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) = updateConfig()
        })
        config.swipeUpAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) = updateConfig()
        })
        config.swipeLeftAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) = updateConfig()
        })
        config.swipeRightAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) = updateConfig()
        })

        config.recentSwipeUpAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) = updateConfig()
        })
        config.recentSwipeDownAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) = updateConfig()
        })
        config.recentSwipeLeftAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) = updateConfig()
        })
        config.recentSwipeRightAction.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) = updateConfig()
        })
    }

    /** Takes care of all logic concerning switching the shown page in the container**/
    private fun loadFragment(itemId: Int) {
        // Find fragment for intended page
        var fragment : Fragment? = supportFragmentManager.findFragmentByTag(getPageTitle(itemId) as String)

        // Start page swap transaction
        val fragTransaction = supportFragmentManager.beginTransaction()

        // Detach current page if there is one
        if (supportFragmentManager.primaryNavigationFragment != null)
            fragTransaction.detach(supportFragmentManager.primaryNavigationFragment!!)

        // Get a new fragment (based on item selected) if frag manager doesn't have one
        if (fragment == null) {
            when (itemId) {
                R.id.navigation_main_options -> { fragment = MainOptionsFragment.newInstance() }
                R.id.navigation_app_actions -> { fragment = AppActionsFragment.newInstance() }
                R.id.navigation_about -> { fragment = HelpFragment.newInstance() }
                else -> fragment = MainOptionsFragment.newInstance()
            }
            fragTransaction.add(R.id.frag_container, fragment, getPageTitle(itemId) as String)
        }
        else
            fragTransaction.attach(fragment)

        fragTransaction.setPrimaryNavigationFragment(fragment)
        fragTransaction.setReorderingAllowed(true)
        fragTransaction.commitNowAllowingStateLoss()
    }

    /** Returns the title of the page corresponding to the page int resource **/
    private fun getPageTitle(page: Int?): CharSequence? {
        return when (page)
        {
            R.id.navigation_main_options -> getString(R.string.title_main_options)
            R.id.navigation_app_actions -> getString(R.string.title_app_actions)
            R.id.navigation_about -> getString(R.string.title_help)
            else -> "ERROR"
        }
    }

    /* ------------------------------------------ Listener Functions ------------------------------------------ */

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

    //TODO fix WaterfallToolbar so that it doesn't crash when rebinding scrolling component
    override fun bindToolbar(recyclerView: RecyclerView) {
        unbindToolbar()
        waterfall_toolbar.recyclerView = recyclerView
    }

    override fun bindToolbar(scrollView: ScrollView) {
        unbindToolbar()
        waterfall_toolbar.scrollView = scrollView
    }

    override fun unbindToolbar() {
        waterfall_toolbar.recyclerView = null
        waterfall_toolbar.scrollView = null
    }

    companion object {
        const val REQ_FINGERPRINT = 88
        const val RES_ACCESSIBILITY = 5
    }
}
