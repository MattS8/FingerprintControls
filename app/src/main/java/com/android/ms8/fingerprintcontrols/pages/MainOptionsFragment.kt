package com.android.ms8.fingerprintcontrols

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.android.ms8.fingerprintcontrols.Configuration.Companion.CONFIG
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_main_options.view.*

class MainOptionsFragment : Fragment(), AdapterView.OnItemSelectedListener {
    /** Implementation of listener function for spinners **/
    override fun onNothingSelected(parent: AdapterView<*>?) {}

    /** Implementation of listener function for spinners **/
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        onActionSelected(view.id, position)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_options, container, false)
            .apply { setCurrentSpinnerValues(this) }
            .apply { setSpinnerListeners(this) }
    }

    /** Returns copy of current configuration file **/
    private fun getConfiguration() : Configuration {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return Gson().fromJson(prefs.getString(CONFIG, Gson().toJson(Configuration())), Configuration::class.java)
    }

    /** Setter for spinner listeners **/
    private fun setSpinnerListeners(view: View) {
        view.spinner_default_swipe_up.onItemSelectedListener = this
        view.spinner_default_swipe_down.onItemSelectedListener = this
        view.spinner_default_swipe_left.onItemSelectedListener = this
        view.spinner_default_swipe_right.onItemSelectedListener = this
    }

    /** Sets all spinner values based on current configuration file **/
    private fun setCurrentSpinnerValues(view: View) {
        view.spinner_default_swipe_up.setSelection(getConfiguration().swipeUpAction)
        view.spinner_default_swipe_down.setSelection(getConfiguration().swipeDownAction)
        view.spinner_default_swipe_left.setSelection(getConfiguration().swipeLeftAction)
        view.spinner_default_swipe_right.setSelection(getConfiguration().swipeRightAction)
    }

    /**
     * Sets the proper variable in the configuration file to the value specified by position and then updates the
     * shared preferences to reflect the changes.
     *
     * This function is called whenever a user selects an option from any spinner.
     */
    private fun onActionSelected(id: Int, position: Int) {
        when (id) {
            R.id.spinner_default_swipe_up -> {
                getConfiguration().swipeUpAction = position}
            R.id.spinner_default_swipe_down -> {
                getConfiguration().swipeDownAction = position}
            R.id.spinner_default_swipe_left -> {
                getConfiguration().swipeLeftAction = position}
            R.id.spinner_default_swipe_right -> {
                getConfiguration().swipeRightAction = position}
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .remove(CONFIG)
            .putString(CONFIG, Gson().toJson(getConfiguration()))
            .apply()
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainOptionsFragment()
    }
}
