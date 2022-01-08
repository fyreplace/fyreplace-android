package app.fyreplace.fyreplace.ui

import android.widget.TextView
import app.fyreplace.protos.Profile

fun TextView.setUsername(profile: Profile) {
    text = context.getUsername(profile)
}
