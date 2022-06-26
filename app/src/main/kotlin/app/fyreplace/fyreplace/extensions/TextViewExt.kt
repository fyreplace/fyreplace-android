package app.fyreplace.fyreplace.extensions

import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Profile

fun TextView.setUsername(profile: Profile) {
    text = profile.getUsername(context)
}

fun TextView.setComment(comment: Comment) {
    text = if (comment.isDeleted) context.getText(R.string.comment_deleted) else comment.text
    alpha = if (comment.isDeleted) 0.6f else 1f
}
