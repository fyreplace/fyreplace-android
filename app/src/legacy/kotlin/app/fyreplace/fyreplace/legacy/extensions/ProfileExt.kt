package app.fyreplace.fyreplace.legacy.extensions

import android.content.Context
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Rank

val Profile?.isAdmin get() = (this?.rank ?: Rank.RANK_CITIZEN) > Rank.RANK_CITIZEN

val Profile.isAvailable get() = !is_banned && username.isNotEmpty()

fun Profile.getUsername(context: Context): CharSequence = when {
    is_deleted -> context.getText(R.string.profile_deleted)
    is_banned -> context.getText(R.string.profile_banned)
    username.isEmpty() -> context.getText(R.string.profile_anonymous)
    else -> username
}
