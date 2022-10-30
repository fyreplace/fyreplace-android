package app.fyreplace.fyreplace.events

import com.google.protobuf.ByteString

class RemoteNotificationReceptionEvent(
    val channel: String,
    val command: String,
    val postId: ByteString
) : Event
