package com.android.ms8.fingerprintcontrols.pages

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Switch
import com.android.ms8.fingerprintcontrols.MainActivity
import com.android.ms8.fingerprintcontrols.R
import com.android.ms8.fingerprintcontrols.databinding.FragmentMainOptionsBinding
import com.android.ms8.fingerprintcontrols.listeners.FragmentListener
import com.android.ms8.fingerprintcontrols.service.FingerprintService

class MainOptionsFragment : Fragment() {
    lateinit var binding: FragmentMainOptionsBinding
    private var listener: FragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = FragmentMainOptionsBinding.inflate(inflater, container, false)
            .apply {
                this.config = listener?.getConfiguration()
                this.serviceEnabled.setOnClickListener { v -> toggleService((v as Switch).isChecked) }
                this.btnToDefaults.setOnClickListener{resetToDefaults()}
                listener?.bindToolbar(this.scrollviewMainOptions)
                binding = this
            }.root

    private fun resetToDefaults() {
        TODO("Not yet implemented")
    }

    /**
     * Starts the service-enabling process when bIsChecked is true and attempts to stop the service
     * when bIsChecked is false
     */
    private fun toggleService(bIsChecked: Boolean) {
        when {
            bIsChecked ->
                activity?.requestPermissions(arrayOf(getFingerprintPermission()), MainActivity.REQ_FINGERPRINT)
            FingerprintService.getServiceObject() != null ->
                activity?.stopService(Intent(context, FingerprintService::class.java))
            else -> Log.w("test###", "Call to toggleService(false) while no FingerprintService is running!")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener?.updateConfig()
        listener?.unbindToolbar()
        listener = null
    }

    /**
     * Updates the configuration file to the value specified and attempts to start the service if the id is
     * R.id.serviceEnabled.
     *
     * This function is called whenever a user taps on a switch.
     */
    private fun onCheckChanged(id: Int, bChecked: Boolean) {
        // Check the id and either enable/disable recent app actions or attempt to start/stop the service
        when (id) {
            R.id.serviceEnabled -> activity?.requestPermissions(arrayOf(getFingerprintPermission()),
                MainActivity.REQ_FINGERPRINT
            )
            else -> binding.config?.bRecentActionsEnabled?.set(false)
        }
    }

    /* ------------------------------------------ Simple helper functions ------------------------------------------ */

    /** Returns USE_BIOMETRIC for devices running android version Q or higher, otherwise returns USE_FINGERPRINT **/
    private fun getFingerprintPermission(): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> Manifest.permission.USE_BIOMETRIC
            else -> Manifest.permission.USE_FINGERPRINT
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = MainOptionsFragment()
    }
}
