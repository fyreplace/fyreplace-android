package app.fyreplace.fyreplace.extensions

import app.fyreplace.protos.Notification
import com.google.protobuf.ByteString

val Notification.id: ByteString
    get() {
        val baseId = when (targetCase) {
            Notification.TargetCase.USER -> user.id
            Notification.TargetCase.POST -> post.id
            Notification.TargetCase.COMMENT -> comment.id
            else -> throw RuntimeException()
        }

        return ByteString.copyFrom(baseId.toByteArray() + (if (isFlag) 1 else 0))
    }
