package com.ms8.fingerprintcontrols.util

import android.content.Context
import android.databinding.BindingAdapter
import android.net.Uri
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.ms8.fingerprintcontrols.R
import com.ms8.fingerprintcontrols.data.Configuration

@BindingAdapter("iconUri")
    fun setIconUri(imageView: ImageView, uri: Uri) {
        Glide.with(imageView.context).load(uri).into(imageView)
    }

    @BindingAdapter("numberOfCustomActions")
    fun setNumberOfCustomActions(textView: TextView, numActions: Int) {
        if (numActions > 0) {
            val str = "$numActions " +
                    if (numActions > 1) textView.context.getString(R.string.custom_actions)
                    else textView.context.getString(R.string.custom_action)
            textView.text = str
            textView.visibility = View.VISIBLE
        }
        else
            textView.visibility = View.INVISIBLE
    }

    @BindingAdapter("numberOfCustomActions")
    fun setNumberOfCustomActions(imageView: ImageView, numActions: Int) {
        imageView.visibility = if (numActions > 0) View.VISIBLE else View.GONE
    }

fun userEnabledService(context: Context): Boolean {
    val configPrefStr = PreferenceManager.getDefaultSharedPreferences(context)
        .getString(Configuration.CONFIG, Gson().toJson(Configuration(null)))
    val config = Gson().fromJson(configPrefStr, Configuration::class.java)
    return config.bUserEnabledService.get()
}
