package com.android.ms8.fingerprintcontrols.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.android.ms8.fingerprintcontrols.data.AppInfo
import com.android.ms8.fingerprintcontrols.databinding.DialogAppActionsBinding

class AppActionsDialog(context: Context, private var appInfo: AppInfo) : Dialog(context) {
    lateinit var binding : DialogAppActionsBinding

    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        binding = DialogAppActionsBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = appInfo
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSaveChanges.setOnClickListener{ saveChanges() }
    }

    private fun saveChanges() {
        // Write new changes to file

        //

        // Dismiss dialog
    }
}