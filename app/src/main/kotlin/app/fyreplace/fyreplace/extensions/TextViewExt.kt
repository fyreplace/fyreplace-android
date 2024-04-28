package app.fyreplace.fyreplace.extensions

import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Profile

fun TextView.setUsername(profile: Profile) {
    text = profile.getUsername(context)
}

fun TextView.setComment(comment: Comment, highlighted: Boolean) {
    setLinkifiedText(if (comment.isDeleted) context.getText(R.string.comment_deleted) else comment.text)
    setTextIsSelectable(!comment.isDeleted)
    alpha = if (comment.isDeleted) 0.6f else 1f
    setTypeface(null, if (highlighted) Typeface.BOLD else Typeface.NORMAL)
}

fun TextView.setLinkifiedText(text: CharSequence) {
    this.text = text
    autoLinkMask = Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS
    linksClickable = true
    movementMethod = LinkMovementMethod()
    setTextIsSelectable(true)
}
