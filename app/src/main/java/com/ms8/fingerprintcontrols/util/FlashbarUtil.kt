package com.ms8.fingerprintcontrols.util

import android.app.Activity
import com.andrognito.flashbar.Flashbar
import com.ms8.fingerprintcontrols.R

object FlashbarUtil {

    fun buildErrorMessage(activity: Activity, titleRes: Int, messageRes: Int, posActionRes: Int) =
        Flashbar.Builder(activity)
            .dismissOnTapOutside()
            .enableSwipeToDismiss()
            .title(titleRes)
            .messageColorRes(R.color.primaryTextColorLight)
            .negativeActionTextColorRes(R.color.colorPrimaryDark)
            .backgroundColorRes(R.color.colorPrimaryLight)
            .positiveActionTextColorRes(R.color.colorAccent)
            .titleColorRes(android.R.color.holo_red_dark)
            .message(messageRes)
            .positiveActionText(posActionRes)
            .positiveActionTapListener(object : Flashbar.OnActionTapListener {
                override fun onActionTapped(bar: Flashbar) = bar.dismiss()
            })

    fun buildNormalMessage(activity: Activity, titleRes: Int, messageRes: Int, posActionRes: Int) =
        Flashbar.Builder(activity)
            .dismissOnTapOutside()
            .enableSwipeToDismiss()
            .title(titleRes)
            .negativeActionTextColorRes(R.color.colorPrimaryDark)
            .messageColorRes(R.color.primaryTextColorLight)
            .backgroundColorRes(R.color.colorPrimaryLight)
            .positiveActionTextColorRes(R.color.colorAccent)
            .titleColorRes(R.color.primaryTextColorLight)
            .message(messageRes)
            .positiveActionText(posActionRes)
            .positiveActionTapListener(object : Flashbar.OnActionTapListener {
                override fun onActionTapped(bar: Flashbar) = bar.dismiss()
            })
}