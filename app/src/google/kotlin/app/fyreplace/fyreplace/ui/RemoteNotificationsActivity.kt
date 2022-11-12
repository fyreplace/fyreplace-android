package app.fyreplace.fyreplace.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.CommentWasCreatedEvent
import app.fyreplace.fyreplace.events.CommentWasDeletedEvent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.RemoteNotificationReceptionEvent
import app.fyreplace.fyreplace.extensions.*
import app.fyreplace.protos.Comment
import app.fyreplace.protos.MessagingService
import app.fyreplace.protos.NotificationServiceGrpcKt
import app.fyreplace.protos.messagingToken
import com.google.firebase.messaging.FirebaseMessaging
import com.google.protobuf.ByteString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import javax.inject.Inject

@AndroidEntryPoint
abstract class RemoteNotificationsActivity(contentLayoutId: Int) :
    AppCompatActivity(contentLayoutId),
    FailureHandler {
    @Inject
    lateinit var em: EventsManager

    @Inject
    lateinit var notificationStub: NotificationServiceGrpcKt.NotificationServiceCoroutineStub

    abstract fun tryHandleCommentCreation(comment: Comment, postId: ByteString): Boolean

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) setupRemoteNotificationChannels()
        }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        em.events.filterIsInstance<RemoteNotificationReceptionEvent>()
            .filter { it.command == "comment:creation" }
            .launchCollect {
                em.post(CommentWasCreatedEvent(it.comment, it.postId, false))

                if (!tryHandleCommentCreation(it.comment, it.postId)) {
                    createNotification(
                        it.comment.notificationTag(it.postId),
                        Intent(Intent.ACTION_VIEW, makeShareUri("p", it.postId)),
                        it.channel,
                        it.comment.author.username,
                        it.comment.text,
                        it.comment.dateCreated.date
                    )
                }
            }
        em.events.filterIsInstance<RemoteNotificationReceptionEvent>()
            .filter { it.command == "comment:deletion" }
            .launchCollect {
                val comment = Comment.newBuilder(it.comment).setIsDeleted(true).build()
                em.post(CommentWasDeletedEvent(comment, it.postId))
            }
    }

    protected fun startReceivingRemoteNotifications() {
        setupRemoteNotificationChannels()
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            launch {
                notificationStub.registerToken(messagingToken {
                    service = MessagingService.MESSAGING_SERVICE_FCM
                    token = it
                })
            }
        }
    }

    protected fun stopReceivingRemoteNotifications() {
        FirebaseMessaging.getInstance().deleteToken()
    }

    protected fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupRemoteNotificationChannels() {
        val commentsGroup = NotificationChannelGroupCompat.Builder(
            getString(R.string.notification_channel_group_comments_id)
        )
            .setName(getString(R.string.notification_channel_group_comments_name))
            .build()

        val ownPostsCommentsChannel = NotificationChannelCompat.Builder(
            getString(R.string.notification_channel_comments_own_posts_id),
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setGroup(getString(R.string.notification_channel_group_comments_id))
            .setName(getString(R.string.notification_channel_comments_own_posts_name))

        val otherPostsCommentsChannel = NotificationChannelCompat.Builder(
            getString(R.string.notification_channel_comments_other_posts_id),
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setGroup(getString(R.string.notification_channel_group_comments_id))
            .setName(getString(R.string.notification_channel_comments_other_posts_name))

        for (builder in listOf(ownPostsCommentsChannel, otherPostsCommentsChannel)) {
            builder.setLightsEnabled(true).setLightColor(getColor(R.color.seed))
        }

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannelGroup(commentsGroup)
        notificationManager.createNotificationChannel(ownPostsCommentsChannel.build())
        notificationManager.createNotificationChannel(otherPostsCommentsChannel.build())
        notificationManager.deleteUnlistedNotificationChannels(
            listOf(
                R.string.notification_channel_comments_own_posts_id,
                R.string.notification_channel_comments_other_posts_id
            ).map(::getString)
        )
    }
}
