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
import android.support.v4.app.FragmentTransaction
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ScrollView
import com.andrognito.flashbar.Flashbar
import com.android.ms8.fingerprintcontrols.data.Configuration
import com.android.ms8.fingerprintcontrols.data.Configuration.Companion.CONFIG
import com.android.ms8.fingerprintcontrols.databinding.MainActivityBinding
import com.android.ms8.fingerprintcontrols.firestore.DatabaseFunctions
import com.android.ms8.fingerprintcontrols.listeners.FragmentListener
import com.android.ms8.fingerprintcontrols.listeners.ObservableListener
import com.android.ms8.fingerprintcontrols.pages.AppActionsFragment
import com.android.ms8.fingerprintcontrols.pages.HelpFragment
import com.android.ms8.fingerprintcontrols.pages.MainOptionsFragment
import com.android.ms8.fingerprintcontrols.service.FingerprintService
import com.android.ms8.fingerprintcontrols.util.ApkInfoFactory
import com.android.ms8.fingerprintcontrols.util.FlashbarUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.android.synthetic.main.main_activity.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), FragmentListener, ObservableListener {
    lateinit var binding : MainActivityBinding
    lateinit var config: Configuration
    val fragments : HashMap<Int, Fragment> = HashMap()
    /**
     * Listener for bottom nav bar that loads the proper fragment.
     */
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            binding.navigation.selectedItemId -> {}
            else -> {
                // Set current selected page
                binding.configuration?.currentPage?.set(item.itemId)

                // Load fragment and return success/failure
                loadFragment(item.itemId)

                // Show/Hide toolbar and set title based on page selected (hide for help page)
                when (item.itemId == R.id.navigation_about) {
                    true -> toolbar.hideToolbar().also { toolbar.title = "" } // Hiding title for help page
                    false -> toolbar.showToolbar().also { toolbar.title = getPageTitle(item.itemId) }
                }
            }
        }

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
                        config.bServiceEnabled.set(false)
                        updateConfig()
                        // Show notification to user
                        FlashbarUtil.buildErrorMessage(this, R.string.error, R.string.err_perm_denied,
                                                        R.string.dismiss)
                            .build()
                            .show()
                        //Snackbar.make(this.container, R.string.err_perm_denied, Snackbar.LENGTH_LONG).show()
                    }

                    // Permission was accepted and fingerprint hardware was found
                    FingerprintManagerCompat.from(this).isHardwareDetected -> {
                        // Update configuration file
                        config.bServiceEnabled.set(true)
                        config.bUserEnabledService.set(true)
                        updateConfig()

                        // Start service if no service is running
                        when (FingerprintService.getServiceObject() == null) {
                            true -> startActivityForResult(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), RES_ACCESSIBILITY)
                            else -> {}
                        }
                    }
                    // Permission was accepted but no fingerprint hardware was found
                    else -> {
                        // Update configuration file
                        config.bServiceEnabled.set(false)
                        updateConfig()
                        // Show notification to user
                        FlashbarUtil.buildErrorMessage(this, R.string.error, R.string.err_enabling, R.string.report_bug)
                            .negativeActionText(R.string.dismiss)
                            .negativeActionTapListener(object : Flashbar.OnActionTapListener {
                                override fun onActionTapped(bar: Flashbar) { bar.dismiss() }
                            })
                            .positiveActionTapListener(object : Flashbar.OnActionTapListener {
                                override fun onActionTapped(bar: Flashbar) {
                                    bar.dismiss()
                                    DatabaseFunctions.sendBugReport("ERROR_ENABLING_SERVICE",
                                        DatabaseFunctions.TYPE_ERR_CRITICAL)
                                        .addOnCompleteListener {
                                            when (it.isSuccessful) {
                                                true -> {
                                                    FlashbarUtil
                                                        .buildNormalMessage(this@MainActivity, R.string.report_sent,
                                                                             R.string.report_sent_desc, R.string.dismiss)
                                                        .build().show()
                                                }
                                                false -> {
                                                    FlashbarUtil
                                                        .buildErrorMessage(this@MainActivity, R.string.error,
                                                                            R.string.error_sending_report, R.string.dismiss)
                                                        .build().show()
                                                }
                                            }
                                        }
                                }
                            })
                            .build().show()
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    override fun onResume() = super.onResume()
        .also {
            FirebaseAuth.getInstance().addAuthStateListener {
                if (it.currentUser == null)
                    it.signInAnonymously()
            }

            config.bServiceEnabled.set(FingerprintService.getServiceObject() != null)
        }

    override fun onPause() = super.onPause()
        .also { updateConfig() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.lifecycleOwner = this

        // Get fragments
        fragments[R.id.navigation_main_options] = MainOptionsFragment.newInstance()
        fragments[R.id.navigation_app_actions] = AppActionsFragment.newInstance()
        fragments[R.id.navigation_about] = HelpFragment.newInstance()

        // Start background task to fetch AppInfo
        ApkInfoFactory.getApps(null, WeakReference(this))

        // Get saved/default configuration file
        val configPrefStr = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(CONFIG, Gson().toJson(Configuration(null)))
        config = Gson().fromJson(configPrefStr, Configuration::class.java)

        // Link binding
        binding.configuration = config

        // Set bottom nav bar listener
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Load last viewed page
        loadFragment(config.currentPage.get())

        // Set bottom nav bar to last viewed page
        navigation.selectedItemId = config.currentPage.get()

        // Set title based on selected page
        toolbar.title = getPageTitle(config.currentPage.get())

        // Add callbacks for spinners to detect changes instantly
        addSpinnerCallbacks()
    }

    /* ------------------------------------------ Simple helper functions ------------------------------------------ */

    /** Adds callback to all spinners that updates config immediately after change **/
    private fun addSpinnerCallbacks() {
        config.bUserEnabledService.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (config.bUserEnabledService.get() && !config.bServiceEnabled.get()) {
                    config.bUserEnabledService.set(false)
                }
                updateConfig()
            }
        })

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
        val fragment : Fragment = fragments[itemId] ?: MainOptionsFragment.newInstance()
            .also { Log.e(TAG, "Expected to find a fragment with id $itemId")}

        // Start page swap transaction
        val fragTransaction = supportFragmentManager.beginTransaction()

        // Add transition animations
        fragTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.no_anim)

        // Replace current fragment with new one
        fragTransaction.replace(R.id.frag_container, fragment, getPageTitle(itemId) as String)

        // Animate fragment entering
        fragTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        fragTransaction.commit()

        // OLD loadFragment()
/*
        // Find fragment for intended page
        var fragment : Fragment? = supportFragmentManager.findFragmentByTag(getPageTitle(itemId) as String)

        // Start page swap transaction
        val fragTransaction = supportFragmentManager.beginTransaction()

        // Detach current page if there is one
        if (supportFragmentManager.primaryNavigationFragment != null)
            fragTransaction.detach(supportFragmentManager.primaryNavigationFragment!!)

        // Get a new fragment (based on item selected) if frag manager doesn't have one
        if (fragment == null) {
            fragment = when (itemId) {
                R.id.navigation_main_options -> MainOptionsFragment.newInstance()
                R.id.navigation_app_actions -> AppActionsFragment.newInstance()
                R.id.navigation_about -> HelpFragment.newInstance()
                else -> MainOptionsFragment.newInstance()
            }
            fragTransaction.add(R.id.frag_container, fragment, getPageTitle(itemId) as String)
        }
        else
            fragTransaction.attach(fragment)

        fragTransaction.setPrimaryNavigationFragment(fragment)
        fragTransaction.setReorderingAllowed(true)

        // Add transition animations
        fragTransaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_bottom)
        fragTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)


        fragTransaction.commitNowAllowingStateLoss()
*/
    }

    /** Returns the title of the page corresponding to the page int resource **/
    private fun getPageTitle(page: Int?): CharSequence? {
        return when (page)
        {
            R.id.navigation_main_options -> getString(R.string.title_main_options)
            R.id.navigation_app_actions -> getString(R.string.title_app_actions)
            R.id.navigation_about -> getString(R.string.title_help)
            else -> {
                Log.e("GetPageTitle", "Unknown id: $page")
                "ERROR"
            }
        }
    }

    /* ------------------------------------------ Listener Functions ------------------------------------------ */

    /** Updates preferences with latest config **/
    override fun updateConfig() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .remove(CONFIG)
            .putString(CONFIG, Gson().toJson(binding.configuration, Configuration::class.java))
            .apply()
    }

    /** A simple getter for the current copy of the configuration file **/
    override fun getConfiguration(): Configuration? {
        return config
    }

    override fun bindToolbar(recyclerView: RecyclerView) {
        // Don't want both ScrollView and RecyclerView bound at the same time!
        unbindToolbarScrollView()

        toolbar.recyclerView = recyclerView
    }

    override fun bindToolbar(scrollView: ScrollView) {
        // Don't want both ScrollView and RecyclerView bound at the same time!
        unbindToolbarRecyclerView()

        toolbar.scrollView = scrollView
    }

    override fun unbindToolbarRecyclerView() { toolbar.recyclerView = null }

    override fun unbindToolbarScrollView() { toolbar.scrollView = null }

    companion object {
        const val TAG = "MainActivity"

        const val REQ_FINGERPRINT = 88
        const val RES_ACCESSIBILITY = 5
    }
}
