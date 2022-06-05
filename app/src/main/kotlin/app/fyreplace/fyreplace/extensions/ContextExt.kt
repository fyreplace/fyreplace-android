package app.fyreplace.fyreplace.extensions

import android.content.Context
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Profile

fun Context.getUsername(profile: Profile): CharSequence = when {
    profile.isBanned -> getText(R.string.banned)
    profile.username.isEmpty() -> getText(R.string.anonymous)
    else -> profile.username
}
