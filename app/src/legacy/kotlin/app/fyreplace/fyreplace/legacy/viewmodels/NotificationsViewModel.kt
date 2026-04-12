package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.ActivityWasStoppedEvent
import app.fyreplace.fyreplace.legacy.events.CommentWasSeenEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.ItemEvent
import app.fyreplace.fyreplace.legacy.events.NotificationWasCreatedEvent
import app.fyreplace.fyreplace.legacy.events.NotificationWasDeletedEvent
import app.fyreplace.fyreplace.legacy.events.NotificationWasUpdatedEvent
import app.fyreplace.fyreplace.legacy.events.RemoteNotificationWasReceivedEvent
import app.fyreplace.fyreplace.legacy.extensions.id
import app.fyreplace.protos.CommentServiceClient
import app.fyreplace.protos.Id
import app.fyreplace.protos.Notification
import app.fyreplace.protos.NotificationServiceClient
import app.fyreplace.protos.Notifications
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceClient
import app.fyreplace.protos.UserServiceClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import okio.ByteString
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class NotificationsViewModel @Inject constructor(
    override val preferences: SharedPreferences,
    em: EventsManager,
    private val notificationService: NotificationServiceClient,
    private val userService: UserServiceClient,
    private val postService: PostServiceClient,
    private val commentService: CommentServiceClient,
) : ItemListViewModel<Notification, Notifications>(em) {
    override val addedItems = emptyFlow<ItemEvent<Notification>>()
    override val updatedItems = em.events.filterIsInstance<NotificationWasUpdatedEvent>()
    override val removedItems = em.events.filterIsInstance<NotificationWasDeletedEvent>()
    override val emptyText = MutableStateFlow(R.string.notifications_empty).asStateFlow()

    init {
        viewModelScope.launch {
            em.events.filterIsInstance<ActivityWasStoppedEvent>().collect { reset() }
        }

        viewModelScope.launch {
            em.events.filterIsInstance<CommentWasSeenEvent>().collect {
                val position = getPosition(Notification(post = Post(id = it.postId)))
                val notification = items.getOrNull(position) ?: return@collect

                if (it.commentsLeft == 0) {
                    em.post(NotificationWasDeletedEvent(notification))
                } else if (notification.count >= it.commentsLeft) {
                    val newNotification = notification.copy(count = it.commentsLeft)
                    em.post(NotificationWasUpdatedEvent(newNotification))
                }
            }
        }

        viewModelScope.launch {
            em.events.filterIsInstance<RemoteNotificationWasReceivedEvent>().collect {
                val position = getPosition(Notification(post = Post(id = it.postId)))
                val notification = items.getOrNull(position)
                val event = when {
                    it.command !in setOf("comment:creation", "comment:deletion") -> return@collect

                    notification == null -> if (it.command == "comment:creation")
                        NotificationWasCreatedEvent(Notification(post = Post(id = it.postId)))
                    else return@collect

                    notification.count == 1 && it.command == "comment:deletion" -> NotificationWasDeletedEvent(
                        notification
                    )

                    else -> NotificationWasUpdatedEvent(
                        notification.copy(count = notification.count + if (it.command == "comment:deletion") -1 else 1)
                    )
                }

                em.post(event)
            }
        }
    }

    override fun getItemId(item: Notification) = item.id

    override fun listItems() = notificationService.List()

    override fun getNextCursor(items: Notifications) = items.next

    override fun getItemList(items: Notifications) = items.notifications

    suspend fun clearAll() = notificationService.Clear().executeFully(Unit)

    suspend fun absolve(notification: Notification) = when {
        notification.user != null -> absolveUser(notification.user.id)
        notification.post != null -> absolvePost(notification.post.id)
        notification.comment != null -> absolveComment(notification.comment.id)
        else -> Unit
    }

    private suspend fun absolveUser(id: ByteString) =
        userService.Absolve().executeFully(Id(id = id))

    private suspend fun absolvePost(id: ByteString) =
        postService.Absolve().executeFully(Id(id = id))

    private suspend fun absolveComment(id: ByteString) =
        commentService.Absolve().executeFully(Id(id = id))
}
