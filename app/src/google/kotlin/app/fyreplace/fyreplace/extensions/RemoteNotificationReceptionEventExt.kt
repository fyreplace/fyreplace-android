package app.fyreplace.fyreplace.extensions

import app.fyreplace.fyreplace.events.RemoteNotificationWasReceivedEvent
import app.fyreplace.protos.Comment

val RemoteNotificationWasReceivedEvent.comment: Comment
    get() = Comment.parseFrom(byteString(message.data["comment"]!!))
