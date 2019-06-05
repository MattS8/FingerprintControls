package com.android.ms8.fingerprintcontrols.pages


import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.andrognito.flashbar.Flashbar
import com.android.ms8.fingerprintcontrols.R
import com.android.ms8.fingerprintcontrols.databinding.FragmentHelpBinding
import com.android.ms8.fingerprintcontrols.firestore.DatabaseFunctions
import com.android.ms8.fingerprintcontrols.util.FlashbarUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_help.view.*
import java.util.*
import kotlin.collections.HashMap


class HelpFragment : Fragment() {
    private lateinit var binding : FragmentHelpBinding
    private lateinit var state : HelpPageState

    /**
     * Transition animation between layouts.
     */
    private val transition = AutoTransition().apply {
        interpolator = DecelerateInterpolator()
        duration = 400
    }

    /* ------------------------ Firebase Functions ------------------------ */

    private fun sendBugReport() {
        val description = binding.editText.extended_edit_text.text.toString()
        if (description.length < 20)
            return

        // Get Error Endpoint
        val bugType = DatabaseFunctions.getErrorTypeFromDropdown(context, binding.suggestionSpinner.selectedItem as String)

        // Send report and show notification on complete
        DatabaseFunctions.sendBugReport(description, bugType).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    binding.editText.extended_edit_text.setText("")
                    showHelpLayout()
                    activity?.let {
                        FlashbarUtil.buildNormalMessage(it, R.string.report_sent, R.string.report_sent_desc,
                                                        R.string.dismiss)
                            .build().show()
                    }
                }
                else -> {
                    activity?.let {
                        FlashbarUtil.buildErrorMessage(it, R.string.error, R.string.error_sending_report,
                                                       R.string.dismiss)
                            .build().show()
                    }
                }
            }
        }
    }

    private fun sendSuggestion() {
        val description = binding.editText.extended_edit_text.text.toString()
        if (description.length < 20)
            return

        // Get Suggestion Endpoint
        val suggestionType = DatabaseFunctions
                .getSuggestionTypeFromDropdown(context, binding.suggestionSpinner.selectedItem as String)

        // Send suggestion and show notification on complete
        DatabaseFunctions.sendSuggestion(description, suggestionType).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    binding.editText.extended_edit_text.setText("")
                    showHelpLayout()
                    activity?.let {
                        FlashbarUtil.buildErrorMessage(it, R.string.suggestion_sent, R.string.suggestion_sent_desc,
                                                       R.string.dismiss)
                    }
                }
                else -> {
                    activity?.let {
                        FlashbarUtil.buildErrorMessage(it, R.string.error, R.string.error_sending_suggestion,
                            R.string.dismiss)
                            .build().show()
                    }
                }
            }
        }
    }

    /* ------------------------ OnClick Functions ------------------------ */

    private fun viewTutorialClicked() {
        activity?.let {
            FlashbarUtil.buildNormalMessage(it, R.string.coming_soon, R.string.coming_soon_desc, R.string.dismiss)
                .build().show()
        }
    }

    private fun reportBugClicked() {
        when (state.layoutState) {
            LayoutState.SUGGESTION -> { sendSuggestion() }
            LayoutState.BUG_REPORT -> { sendBugReport() }
            LayoutState.HELP -> { showBugReportLayout() }
        }
    }

    private fun sendSuggestionClicked() {
        when (state.layoutState) {
            LayoutState.SUGGESTION -> { showHelpLayout() }
            LayoutState.BUG_REPORT -> { showHelpLayout() }
            LayoutState.HELP -> { showSuggestionLayout() }
        }
    }

    /* ------------------------ Layout Transition Functions ------------------------ */

    private fun showSuggestionLayout() {
        Log.d(TAG, "Showing showSuggestionLayout")

        state.layoutState = LayoutState.SUGGESTION

        // Disable and hide unused views
        binding.viewTutorial.isEnabled = false

        // Enable buttons
        binding.suggestionSpinner.isEnabled = true
        binding.editText.isEnabled = true

        // Set text view texts
        binding.aboutAppTitle.setText(R.string.submit_suggestion_title)
        binding.aboutDesc.setText(R.string.submit_suggestion_desc)
        binding.suggestionsTitle.setText(R.string.suggestion_type)
        binding.sendSuggestion.setText(android.R.string.cancel)
        binding.reportBug.setText(R.string.send)

        // Set hint text
        when (context?.resources?.configuration?.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {binding.suggestionSpinner.hint = getString(R.string.suggestion_type)}
            Configuration.ORIENTATION_PORTRAIT -> {binding.suggestionSpinner.hint = ""}
        }

        // Set spinner values
        context?.let {
            val items = LinkedList<String>(it.resources?.getStringArray(R.array.suggestion_types)?.toList()
                ?: Arrays.asList("Feature Request", "Usability Improvements", "Language Translation", "Other"))
            binding.suggestionSpinner.attachDataSource(items)
        }


        val outerConstraint = ConstraintSet().apply {
            clone(context, R.layout.fragment_help_suggestion)
            setAlpha(binding.bugsTitle.id, 0f)
        }

        TransitionManager.beginDelayedTransition(binding.helpContainer, transition)
        outerConstraint.applyTo(binding.helpContainer)
    }

    private fun showBugReportLayout() {
        Log.d(TAG, "Showing showBugReportLayout")

        state.layoutState = LayoutState.BUG_REPORT

        // Disable and hide unused views
        binding.viewTutorial.isEnabled = false

        // Enable views
        binding.suggestionSpinner.isEnabled = true
        binding.editText.isEnabled = true

        // Set text view texts
        binding.aboutAppTitle.setText(R.string.submit_bug_title)
        binding.aboutDesc.setText(R.string.submit_but_desc)
        binding.bugsTitle.setText(R.string.bug_severity)
        binding.sendSuggestion.setText(android.R.string.cancel)
        binding.reportBug.setText(R.string.send)

        // Set hint text
        when (context?.resources?.configuration?.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {binding.suggestionSpinner.hint = getString(R.string.bug_severity)}
            Configuration.ORIENTATION_PORTRAIT -> {binding.suggestionSpinner.hint = ""}
        }

        // Set spinner values
        context?.let {
            val items = LinkedList<String>(it.resources?.getStringArray(R.array.bug_severity)?.toList()
                ?: Arrays.asList("Critical (App Crashes)", "Major (App Malfunctions/ Unusable)", "Moderate/Low (Graphical glitches or minor hiccups)"))
            binding.suggestionSpinner.attachDataSource(items)
        }

        val outerConstraint = ConstraintSet().apply {
            clone(context, R.layout.fragment_help_bugs)
        }

        TransitionManager.beginDelayedTransition(binding.helpContainer, transition)
        outerConstraint.applyTo(binding.helpContainer)
    }

    private fun showHelpLayout() {
        Log.d(TAG, "Showing showHelpLayout")
        state.layoutState = LayoutState.HELP

        // Enable and show views
        binding.viewTutorial.isEnabled = true

        // Disable unusable buttons
        binding.suggestionSpinner.isEnabled = false
        binding.editText.isEnabled = false

        // Set text view texts
        binding.aboutAppTitle.setText(R.string.how_do_i_use_this_app)
        binding.aboutDesc.setText(R.string.about_desc)
        binding.sendSuggestion.setText(android.R.string.cancel)
        binding.sendSuggestion.setText(R.string.send_suggestion)
        binding.reportBug.setText(R.string.report_bug)
        binding.suggestionsTitle.setText(R.string.suggestions)
        binding.bugsTitle.setText(R.string.found_a_bug)

        val outerConstraint = ConstraintSet().apply {
            clone(context, R.layout.fragment_help)
            constrainHeight(binding.suggestionSpinner.id, 0)
            constrainHeight(binding.editText.id, 0)
        }

        TransitionManager.beginDelayedTransition(binding.helpContainer, transition)
        outerConstraint.applyTo(binding.helpContainer)
        binding.suggestionSpinner.alpha = 0.0f
    }

    /* ------------------------ Overridden Functions ------------------------ */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentHelpBinding.inflate(inflater, container, false).apply {
            binding = this
            lifecycleOwner = this@HelpFragment
            state = HelpPageState().apply {
                layoutState =  LayoutState.valueOf(savedInstanceState?.getString("STATE_LAYOUT", LayoutState.HELP.name) ?: LayoutState.HELP.name)
            }

            Log.d(TAG, "layouState = ${state.layoutState}")

            // Bind onClick listeners
            binding.sendSuggestion.setOnClickListener { sendSuggestionClicked() }
            binding.reportBug.setOnClickListener { reportBugClicked() }
            binding.viewTutorial.setOnClickListener { viewTutorialClicked() }

            // Set layout based on saved state
            when (state.layoutState) {
                LayoutState.HELP -> {showHelpLayout()}
                LayoutState.BUG_REPORT -> {showBugReportLayout()}
                LayoutState.SUGGESTION -> {showSuggestionLayout()}
            }
        }.root

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("STATE_LAYOUT", state.layoutState.name)
    }

    /* ------------------------ Static Stuff and Classes ------------------------ */

    class HelpPageState {
        var layoutState = LayoutState.HELP
    }

    enum class LayoutState {HELP, SUGGESTION, BUG_REPORT}

    companion object {
        @JvmStatic
        fun newInstance() = HelpFragment()

        const val TAG = "HelpFragment"
    }
}
