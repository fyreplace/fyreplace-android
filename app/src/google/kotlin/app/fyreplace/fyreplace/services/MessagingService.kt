package app.fyreplace.fyreplace.services

import android.content.Intent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.RemoteNotificationReceptionEvent
import app.fyreplace.fyreplace.extensions.*
import app.fyreplace.protos.Comment
import app.fyreplace.protos.MessagingService
import app.fyreplace.protos.NotificationServiceGrpcKt
import app.fyreplace.protos.messagingToken
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var em: EventsManager

    @Inject
    lateinit var notificationStub: NotificationServiceGrpcKt.NotificationServiceCoroutineStub

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
                notificationStub.registerToken(messagingToken {
                    service = MessagingService.MESSAGING_SERVICE_FCM
                    this.token = token
                })
            } catch (_: StatusException) {
            } catch (_: StatusRuntimeException) {
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val command = message.data["_command"] ?: return
        val channel = message.data["_fcm.channel"]
        val comment = Comment.parseFrom(byteString(message.data["comment"] ?: return))
        val postId = byteString(message.data["postId"] ?: return)

        when (command) {
            "comment:creation" -> {
                if (!application.current.isInForeground) {
                    createNotification(
                        comment.notificationTag(postId),
                        Intent(Intent.ACTION_VIEW, makeShareUri("p", postId)),
                        channel,
                        comment.author.username,
                        comment.text,
                        comment.dateCreated.date
                    )
                }
            }
            "comment:deletion" -> deleteNotification(comment.notificationTag(postId))
            "comment:acknowledgement" -> deleteNotifications(
                Regex("${postId.base64ShortString}:.*"),
                comment.dateCreated.date
            )
        }

        em.post(RemoteNotificationReceptionEvent(message, channel, command, postId))
    }
}
