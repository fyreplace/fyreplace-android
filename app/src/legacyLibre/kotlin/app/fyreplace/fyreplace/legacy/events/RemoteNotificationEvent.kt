package app.fyreplace.fyreplace.legacy.events

import okio.ByteString

class RemoteNotificationWasReceivedEvent(
    val channel: String,
    val command: String,
    val postId: ByteString
) : Event
