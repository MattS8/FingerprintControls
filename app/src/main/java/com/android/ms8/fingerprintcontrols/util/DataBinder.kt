package com.android.ms8.fingerprintcontrols.util

import android.databinding.BindingAdapter
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.android.ms8.fingerprintcontrols.R
import com.bumptech.glide.Glide

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
