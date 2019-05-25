package com.android.ms8.fingerprintcontrols.pages


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.ms8.fingerprintcontrols.R
import com.android.ms8.fingerprintcontrols.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {
    private lateinit var binding : FragmentHelpBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentHelpBinding.inflate(inflater, container, false).apply {
            binding = this
            lifecycleOwner = this@HelpFragment
            binding.sendSuggestion.setOnClickListener { sendSuggestionClicked() }
            binding.reportBug.setOnClickListener { reportBugClicked() }
            binding.viewTutorial.setOnClickListener { viewTutorialClicked() }
        }.root

    private fun viewTutorialClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun reportBugClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun sendSuggestionClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        @JvmStatic
        fun newInstance() = HelpFragment()
    }
}
