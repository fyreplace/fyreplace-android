package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.*
import app.fyreplace.fyreplace.extensions.id
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    override val updatedItems = merge(
        em.events.filterIsInstance<CommentSeenEvent>()
            .filter { it.commentsLeft > 0 }
            .map {
                val position = getPosition(notification { post = post { id = it.postId } })
                val notification = items.getOrNull(position) ?: return@map null

                if (notification.count < it.commentsLeft) {
                    return@map null
                }

                val newNotification = Notification.newBuilder(notification)
                    .setCount(it.commentsLeft)
                    .build()
                return@map NotificationUpdateEvent(newNotification)
            }
            .filterNotNull(),
        em.events.filterIsInstance()
    )
    override val removedItems = merge(
        em.events.filterIsInstance<CommentSeenEvent>()
            .filter { it.commentsLeft == 0 }
            .map {
                val position = getPosition(notification { post = post { id = it.postId } })
                val notification = items.getOrNull(position) ?: return@map null
                return@map NotificationDeletionEvent(notification)
            }
            .filterNotNull(),
        em.events.filterIsInstance()
    )
    override val emptyText = MutableStateFlow(R.string.notifications_empty)

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
