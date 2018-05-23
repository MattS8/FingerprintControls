package com.android.matt.fingerprintcontrols

import android.animation.Animator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.sheet_feedback_type.*


class FeedbackBottomSheet : BottomSheetDialogFragment() {
    var inputVisible: Boolean = false
    var feedbackType = TYPE_SUGGESTION

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sheet_feedback_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSuggestFeature.setOnClickListener {
            if (!inputVisible) {
                showFeedbackLayout()
            }
            etInput.setHint(R.string.suggestion_hint)
            etInput.setText("")
            feedbackType = TYPE_SUGGESTION
        }
        btnReportBug.setOnClickListener {
            if (!inputVisible) {
                showFeedbackLayout()
            }
            etInput.setHint(R.string.report_bug_hint)
            etInput.setText("")
            feedbackType = TYPE_BUG
        }
        btnSend.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayListOf("Matthew.steinhardt@gmail.com"))
            intent.putExtra(Intent.EXTRA_TEXT, etInput.text.toString())
            if (feedbackType == TYPE_SUGGESTION) {
                intent.putExtra(Intent.EXTRA_SUBJECT, "[Fingerprint Controls] - Suggestion")
            } else {
                intent.putExtra(Intent.EXTRA_SUBJECT, "[Fingerprint Controls] - Bug Report")
            }
            activity?.startActivityForResult(Intent.createChooser(intent, getString(R.string.send_feedback_chooser)), MainActivity.RES_FEEDBACK)
            hideFeedbackLayout()
        }
    }

    private fun showFeedbackLayout() {
        inputLayout.visibility = View.VISIBLE
        inputLayout.animate()
                .translationY(inputLayout.height.toFloat())
                .alpha(1.0f)
                .setListener(null)
        btnSuggestFeature.visibility = View.VISIBLE
        btnSend.visibility = View.VISIBLE
        btnSend.animate()
                .translationY(btnSend.height.toFloat())
                .alpha(1.0f)
                .setListener(null)
        inputVisible = true
    }

    private fun hideFeedbackLayout() {
        inputLayout.animate()
                .translationY(inputLayout.height.toFloat())
                .alpha(1.0f)
                .setListener(object: Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {}

                    override fun onAnimationCancel(p0: Animator?) {
                        inputLayout.visibility = View.GONE
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        inputLayout.visibility = View.GONE
                    }
                })
        btnSuggestFeature.visibility = View.VISIBLE
        btnSend.visibility = View.VISIBLE
        btnSend.animate()
                .translationY(btnSend.height.toFloat())
                .alpha(1.0f)
                .setListener(object:  Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {}

                    override fun onAnimationCancel(p0: Animator?) {
                        btnSend.visibility = View.GONE
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        btnSend.visibility = View.GONE
                    }
                })
        inputVisible = false
    }

    companion object {
        const val TYPE_SUGGESTION = 1
        const val TYPE_BUG = 2
        fun newInstance(): FeedbackBottomSheet =
                FeedbackBottomSheet().apply {}

    }
}
