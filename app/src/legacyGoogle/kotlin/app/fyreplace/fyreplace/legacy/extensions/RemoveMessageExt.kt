package app.fyreplace.fyreplace.legacy.extensions

import app.fyreplace.protos.Comment
import com.google.firebase.messaging.RemoteMessage

fun RemoteMessage.parseComment(): Comment? {
    val encodedComment = data["comment"] ?: return null
    return Comment.ADAPTER.decode(byteString(encodedComment))
}
