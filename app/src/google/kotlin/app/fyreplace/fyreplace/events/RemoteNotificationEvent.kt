package app.fyreplace.fyreplace.events

import com.google.firebase.messaging.RemoteMessage
import com.google.protobuf.ByteString

class RemoteNotificationReceptionEvent(
    val message: RemoteMessage,
    val channel: String?,
    val command: String,
    val postId: ByteString
) : Event
