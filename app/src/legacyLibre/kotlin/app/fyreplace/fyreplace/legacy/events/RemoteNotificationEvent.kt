package app.fyreplace.fyreplace.legacy.events

import com.google.protobuf.ByteString

class RemoteNotificationWasReceivedEvent(
    val channel: String,
    val command: String,
    val postId: ByteString
) : Event
