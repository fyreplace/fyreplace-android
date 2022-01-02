package app.fyreplace.client.ui

import android.content.Context
import app.fyreplace.client.R

fun Context.getUsername(username: String) =
    if (username.isNotEmpty()) username else getText(R.string.anonymous)
