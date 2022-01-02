package app.fyreplace.client.ui

import android.widget.TextView

fun TextView.setUsername(username: String) {
    text = context.getUsername(username)
}
