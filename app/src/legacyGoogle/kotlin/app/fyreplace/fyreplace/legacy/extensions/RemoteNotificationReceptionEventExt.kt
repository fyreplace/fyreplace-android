package app.fyreplace.fyreplace.legacy.extensions

import app.fyreplace.fyreplace.legacy.events.RemoteNotificationWasReceivedEvent
import app.fyreplace.protos.Comment

val RemoteNotificationWasReceivedEvent.comment: Comment
    get() = Comment.parseFrom(byteString(message.data["comment"]!!))
