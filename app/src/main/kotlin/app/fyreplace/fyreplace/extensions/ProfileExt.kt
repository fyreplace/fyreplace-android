package app.fyreplace.fyreplace.extensions

import android.content.Context
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Profile
import app.fyreplace.protos.Rank

val Profile?.isAdmin get() = (this?.rank ?: Rank.RANK_CITIZEN) > Rank.RANK_CITIZEN

val Profile.isAvailable get() = !isBanned && username.isNotEmpty()

fun Profile.getUsername(context: Context): CharSequence = when {
    isDeleted -> context.getText(R.string.profile_deleted)
    isBanned -> context.getText(R.string.profile_banned)
    username.isEmpty() -> context.getText(R.string.profile_anonymous)
    else -> username
}
