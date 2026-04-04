package app.fyreplace.fyreplace.legacy.extensions

import app.fyreplace.protos.Comment
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.Moshi

fun RemoteMessage.parseComment(moshi: Moshi): Comment? {
    return moshi.adapter(Comment::class.java).fromJson(data["comment"] ?: return null)
}
