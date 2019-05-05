package com.android.ms8.fingerprintcontrols.util

import android.content.ContentResolver
import android.databinding.BindingAdapter
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

class DataBinder {

    @BindingAdapter("iconUri")
    fun setIconUri(imageView: ImageView, uri: Uri) {
        Glide.with(imageView.context).load(uri).into(imageView)
    }
}