package app.fyreplace.fyreplace.extensions

import android.widget.TextView
import app.fyreplace.fyreplace.extensions.getUsername
import app.fyreplace.protos.Profile

fun TextView.setUsername(profile: Profile) {
    text = profile.getUsername(context)
}
