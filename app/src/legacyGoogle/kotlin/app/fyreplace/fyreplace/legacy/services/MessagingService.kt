package app.fyreplace.fyreplace.legacy.services

import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.RemoteNotificationWasReceivedEvent
import app.fyreplace.fyreplace.legacy.extensions.authenticate
import app.fyreplace.fyreplace.legacy.extensions.base64ShortString
import app.fyreplace.fyreplace.legacy.extensions.byteString
import app.fyreplace.fyreplace.legacy.extensions.dedupe
import app.fyreplace.fyreplace.legacy.extensions.deleteNotification
import app.fyreplace.fyreplace.legacy.extensions.deleteNotifications
import app.fyreplace.fyreplace.legacy.extensions.mainPreferences
import app.fyreplace.fyreplace.legacy.extensions.notificationTag
import app.fyreplace.fyreplace.legacy.extensions.parseComment
import app.fyreplace.protos.MessagingService
import app.fyreplace.protos.MessagingToken
import app.fyreplace.protos.NotificationServiceClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.wire.GrpcException
import com.squareup.wire.Instant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var em: EventsManager

    @Inject
    lateinit var notificationService: NotificationServiceClient

    private lateinit var lifecycleScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        lifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onDestroy() {
        lifecycleScope.cancel()
        super.onDestroy()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        lifecycleScope.launch {
            try {
                notificationService.RegisterToken().authenticate(mainPreferences).dedupe().execute(
                    MessagingToken(
                        service = MessagingService.MESSAGING_SERVICE_FCM,
                        token = token
                    )
                )
            } catch (_: GrpcException) {
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val command = message.data["_command"] ?: return

        if (command == "notifications:clear") {
            return deleteNotifications()
        }

        val channel = message.data["_fcm.channel"]
        val comment = message.parseComment() ?: return
        val postId = byteString(message.data["postId"] ?: return)

        when (command) {
            "comment:deletion" -> deleteNotification(comment.notificationTag(postId))
            "comment:acknowledgement" -> deleteNotifications(
                Regex("${postId.base64ShortString}:.*"),
                comment.date_created ?: Instant.now()
            )
        }

        em.post(RemoteNotificationWasReceivedEvent(message, channel, command, postId))
    }
}
