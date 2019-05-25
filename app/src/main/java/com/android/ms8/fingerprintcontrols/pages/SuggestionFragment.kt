package com.android.ms8.fingerprintcontrols.pages

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.ms8.fingerprintcontrols.databinding.FragmentDialogSuggestionBinding
import com.android.ms8.fingerprintcontrols.firestore.DatabaseFunctions

class SuggestionFragment : DialogFragment() {
    lateinit var binding : FragmentDialogSuggestionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        FragmentDialogSuggestionBinding.inflate(inflater, container, false)
            .also {
                binding = it
            }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = super.onViewCreated(view, savedInstanceState)
        .also {
            binding.cancelBtn.setOnClickListener { dismiss() }
            binding.sendBtn.setOnClickListener { sendSuggestion() }
    }

    private fun sendSuggestion() {
        //todo ask user if they are sure they want to sent

        // Check for input errors
        if (binding.editText.isOnError)
            return

        // Get suggestion type enum
        val suggestionType = when (binding.suggestionSpinner.selectedItemPosition) {
            0 -> DatabaseFunctions.SuggestionType.FEATURE_REQUEST
            1 -> DatabaseFunctions.SuggestionType.USABILITY_IMPROVEMENT
            2 -> DatabaseFunctions.SuggestionType.LANGUAGE_TRANSLATION
            3 -> DatabaseFunctions.SuggestionType.OTHER
            else -> DatabaseFunctions.SuggestionType.OTHER
                .also { Log.e(TAG, "Unknown suggestion type (spinner position = ${binding.suggestionSpinner.selectedItemPosition}") }
        }

        // Send suggestion to firestore
        DatabaseFunctions.SendSuggestion(binding.extendedEditText.text.toString(), suggestionType)

        // Dismiss
        dismiss()
    }

    companion object {
        const val TAG = "SuggestionFragment"
    }

}