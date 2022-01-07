package app.fyreplace.fyreplace.ui

import android.content.Context
import app.fyreplace.fyreplace.R

fun Context.getUsername(username: String) =
    if (username.isNotEmpty()) username else getText(R.string.anonymous)
