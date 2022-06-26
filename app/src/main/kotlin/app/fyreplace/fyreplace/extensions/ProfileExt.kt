package app.fyreplace.fyreplace.extensions

import android.content.Context
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Profile

val Profile.isAvailable get() = !isBanned && username.isNotEmpty()

fun Profile.getUsername(context: Context): CharSequence = when {
    isBanned -> context.getText(R.string.banned)
    username.isEmpty() -> context.getText(R.string.anonymous)
    else -> username
}
