package app.fyreplace.fyreplace.extensions

import app.fyreplace.fyreplace.events.RemoteNotificationReceptionEvent
import app.fyreplace.protos.Comment

val RemoteNotificationReceptionEvent.comment: Comment
    get() = Comment.parseFrom(byteString(message.data["comment"]!!))
