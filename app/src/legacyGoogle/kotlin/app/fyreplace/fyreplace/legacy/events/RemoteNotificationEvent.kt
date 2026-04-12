package app.fyreplace.fyreplace.legacy.events

import com.google.firebase.messaging.RemoteMessage
import okio.ByteString

class RemoteNotificationWasReceivedEvent(
    val message: RemoteMessage,
    val channel: String?,
    val command: String,
    val postId: ByteString
) : Event
