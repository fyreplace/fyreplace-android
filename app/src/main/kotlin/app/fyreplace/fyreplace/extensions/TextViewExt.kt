package app.fyreplace.fyreplace.extensions

import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextLinks
import android.widget.TextView
import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Comment
import app.fyreplace.protos.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun TextView.setUsername(profile: Profile) {
    text = profile.getUsername(context)
}

suspend fun TextView.setComment(comment: Comment) {
    setLinkifiedText(if (comment.isDeleted) context.getText(R.string.comment_deleted) else comment.text)
    setTextIsSelectable(!comment.isDeleted)
    alpha = if (comment.isDeleted) 0.6f else 1f
}

suspend fun TextView.setLinkifiedText(text: CharSequence) {
    linksClickable = true
    movementMethod = LinkMovementMethod()
    setTextIsSelectable(true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val service = context.getSystemService(Context.TEXT_CLASSIFICATION_SERVICE)
        val textClassificationManager = service as TextClassificationManager
        val links = withContext(Dispatchers.IO) {
            val request = TextLinks.Request.Builder(text).build()
            textClassificationManager.textClassifier.generateLinks(request)
        }
        val spannableText = SpannableString(text)
        links.apply(spannableText, TextLinks.APPLY_STRATEGY_REPLACE, null)
        this.text = spannableText
        movementMethod = LinkMovementMethod()
    } else {
        autoLinkMask = Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS
        this.text = text
    }
}
