package com.github.insanusmokrassar.AndroidFSMConfigConverter.frontend.extensions

import android.view.View
import com.github.insanusmokrassar.AndroidFSMConfigConverter.R

fun View.showProgressBar() {
    findViewById<View>(R.id.progressBar)?.let {
        it.visibility = View.VISIBLE
    }
}

fun View.hideProgressBar() {
    findViewById<View>(R.id.progressBar)?.let {
        it.visibility = View.GONE
    }
}
