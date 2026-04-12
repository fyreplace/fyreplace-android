package app.fyreplace.fyreplace.legacy.extensions

import app.fyreplace.protos.Notification
import okio.ByteString
import okio.ByteString.Companion.toByteString

val Notification.id: ByteString
    get() {
        val baseId = when {
            user != null -> user.id
            post != null -> post.id
            comment != null -> comment.id
            else -> throw RuntimeException()
        }

        return (baseId.toByteArray() + (if (is_flag) 1 else 0)).toByteString()
    }
