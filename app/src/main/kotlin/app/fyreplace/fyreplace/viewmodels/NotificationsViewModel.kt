package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.*
import app.fyreplace.fyreplace.extensions.id
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class NotificationsViewModel @Inject constructor(
    em: EventsManager,
    private val notificationStub: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
    private val userStub: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub,
    private val commentStub: CommentServiceGrpcKt.CommentServiceCoroutineStub,
) :
    ItemListViewModel<Notification, Notifications>(em) {
    override val addedItems = emptyFlow<ItemEvent<Notification>>()
    override val updatedItems = em.events.filterIsInstance<NotificationWasUpdatedEvent>()
    override val removedItems = em.events.filterIsInstance<NotificationWasDeletedEvent>()
    override val emptyText = MutableStateFlow(R.string.notifications_empty)

    init {
        viewModelScope.launch {
            em.events.filterIsInstance<CommentWasSeenEvent>().collect {
                val position = getPosition(notification { post = post { id = it.postId } })
                val notification = items.getOrNull(position) ?: return@collect

                if (it.commentsLeft == 0) {
                    em.post(NotificationWasDeletedEvent(notification))
                } else if (notification.count >= it.commentsLeft) {
                    val newNotification = Notification.newBuilder(notification)
                        .setCount(it.commentsLeft)
                        .build()
                    em.post(NotificationWasUpdatedEvent(newNotification))
                }
            }
        }

        viewModelScope.launch {
            em.events.filterIsInstance<RemoteNotificationReceptionEvent>().collect {
                val position = getPosition(notification { post = post { id = it.postId } })
                val notification = items.getOrNull(position)
                val event = when {
                    it.command !in setOf("comment:creation", "comment:deletion") -> return@collect

                    notification == null -> NotificationWasCreatedEvent(notification {
                        post = post { id = it.postId }
                    })

                    notification.count == 1 && it.command == "comment:deletion" -> NotificationWasDeletedEvent(
                        notification
                    )

                    else -> NotificationWasUpdatedEvent(
                        Notification.newBuilder(notification)
                            .setCount(notification.count + if (it.command == "comment:deletion") -1 else 1)
                            .build()
                    )
                }

                em.post(event)
            }
        }
    }

    override fun getItemId(item: Notification): ByteString = item.id

    override fun listItems() = notificationStub.list(pages)

    override fun hasNextCursor(items: Notifications) = items.hasNext()

    override fun getNextCursor(items: Notifications): Cursor = items.next

    override fun getItemList(items: Notifications): List<Notification> = items.notificationsList

    suspend fun absolve(notification: Notification) = when (notification.targetCase) {
        Notification.TargetCase.USER -> absolveUser(notification.user.id)
        Notification.TargetCase.POST -> absolvePost(notification.post.id)
        Notification.TargetCase.COMMENT -> absolveComment(notification.comment.id)
        else -> Unit
    }

    private suspend fun absolveUser(id: ByteString) {
        userStub.absolve(id { this.id = id })
    }

    private suspend fun absolvePost(id: ByteString) {
        postStub.absolve(id { this.id = id })
    }

    private suspend fun absolveComment(id: ByteString) {
        commentStub.absolve(id { this.id = id })
    }
}
