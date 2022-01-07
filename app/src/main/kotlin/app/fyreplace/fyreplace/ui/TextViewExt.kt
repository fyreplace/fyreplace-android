package app.fyreplace.fyreplace.ui

import android.widget.TextView

fun TextView.setUsername(username: String) {
    text = context.getUsername(username)
}
