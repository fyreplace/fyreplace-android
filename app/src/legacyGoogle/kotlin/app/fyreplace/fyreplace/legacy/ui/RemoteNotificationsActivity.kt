package app.fyreplace.fyreplace.legacy.ui

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
import app.fyreplace.fyreplace.legacy.events.CommentWasCreatedEvent
import app.fyreplace.fyreplace.legacy.events.CommentWasDeletedEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.RemoteNotificationWasReceivedEvent
import app.fyreplace.fyreplace.legacy.extensions.authenticate
import app.fyreplace.fyreplace.legacy.extensions.createNotification
import app.fyreplace.fyreplace.legacy.extensions.dedupe
import app.fyreplace.fyreplace.legacy.extensions.mainPreferences
import app.fyreplace.fyreplace.legacy.extensions.makeShareUri
import app.fyreplace.fyreplace.legacy.extensions.notificationTag
import app.fyreplace.fyreplace.legacy.extensions.parseComment
import app.fyreplace.protos.Comment
import app.fyreplace.protos.MessagingService
import app.fyreplace.protos.MessagingToken
import app.fyreplace.protos.NotificationServiceClient
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.wire.Instant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import okio.ByteString
import javax.inject.Inject

@AndroidEntryPoint
abstract class RemoteNotificationsActivity(contentLayoutId: Int) :
    AppCompatActivity(contentLayoutId),
    FailureHandler {
    @Inject
    lateinit var em: EventsManager

    @Inject
    lateinit var notificationStub: NotificationServiceClient

    abstract fun tryHandleCommentCreation(comment: Comment, postId: ByteString): Boolean

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) setupRemoteNotificationChannels()
        }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        em.events.filterIsInstance<RemoteNotificationWasReceivedEvent>()
            .filter { it.command == "comment:creation" }
            .launchCollect {
                val comment = it.message.parseComment() ?: return@launchCollect
                em.post(CommentWasCreatedEvent(comment, it.postId, false))

                if (!tryHandleCommentCreation(comment, it.postId)) {
                    createNotification(
                        comment.notificationTag(it.postId),
                        Intent(Intent.ACTION_VIEW, makeShareUri("p", it.postId)),
                        it.channel,
                        comment.author?.username.orEmpty(),
                        comment.text,
                        comment.date_created ?: Instant.now()
                    )
                }
            }
        em.events.filterIsInstance<RemoteNotificationWasReceivedEvent>()
            .filter { it.command == "comment:deletion" }
            .launchCollect {
                val comment = (it.message.parseComment() ?: return@launchCollect)
                    .copy(is_deleted = true)
                em.post(CommentWasDeletedEvent(comment, it.postId))
            }
    }

    protected fun startReceivingRemoteNotifications() {
        setupRemoteNotificationChannels()
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            launch {
                notificationStub.RegisterToken().authenticate(mainPreferences).dedupe().execute(
                    MessagingToken(
                        service = MessagingService.MESSAGING_SERVICE_FCM,
                        token = it
                    )
                )
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
            .setVibrationEnabled(true)

        val otherPostsCommentsChannel = NotificationChannelCompat.Builder(
            getString(R.string.notification_channel_comments_other_posts_id),
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setGroup(getString(R.string.notification_channel_group_comments_id))
            .setName(getString(R.string.notification_channel_comments_other_posts_name))

        for (builder in listOf(ownPostsCommentsChannel, otherPostsCommentsChannel)) {
            builder.setShowBadge(true)
                .setShowBadge(true)
                .setLightsEnabled(true)
                .setLightColor(getColor(R.color.seed))
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
