package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.legacy.events.CommentWasCreatedEvent
import app.fyreplace.fyreplace.legacy.events.CommentWasDeletedEvent
import app.fyreplace.fyreplace.legacy.events.CommentWasSavedEvent
import app.fyreplace.fyreplace.legacy.events.CommentWasSeenEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.protos.Comment
import app.fyreplace.protos.CommentServiceClient
import app.fyreplace.protos.Comments
import app.fyreplace.protos.Id
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceClient
import app.fyreplace.protos.Subscription
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import okio.ByteString
import kotlin.math.min

@SuppressLint("CheckResult")
class PostViewModel @AssistedInject constructor(
    override val preferences: SharedPreferences,
    em: EventsManager,
    private val postService: PostServiceClient,
    private val commentService: CommentServiceClient,
    @Assisted initialPost: Post
) : ItemRandomAccessListViewModel<Comment, Comments>(em, initialPost.id) {
    override val addedItems = em.events.filterIsInstance<CommentWasCreatedEvent>()
        .filter { it.postId == post.value.id }
    override val updatedItems = em.events.filterIsInstance<CommentWasDeletedEvent>()
        .filter { it.postId == post.value.id }
    private val mPost = MutableStateFlow(initialPost)
    private val mSubscribed = MutableStateFlow(initialPost.is_subscribed)
    private var mSelectedComment = MutableStateFlow<Int?>(null)
    private var mShouldScrollToComment = true
    private var mSavedComment = ""
    private var acknowledgedPosition = -1
    val post = mPost.asStateFlow()
    val subscribed = mSubscribed.asStateFlow()
    val selectedComment = mSelectedComment.asStateFlow()
    val shouldScrollToComment get() = mShouldScrollToComment
    val scrollTargetPosition
        get() = selectedComment.value ?: min(post.value.comments_read, totalSize)
    val savedComment get() = mSavedComment

    init {
        viewModelScope.launch {
            em.events.filterIsInstance<CommentWasSavedEvent>()
                .collect { mSavedComment = it.text }
        }

        viewModelScope.launch {
            em.events.filterIsInstance<CommentWasCreatedEvent>()
                .filter { it.postId == post.value.id }
                .collect {
                    mPost.value = post.value.copy(comments_read = post.value.comments_read + 1)
                    mSubscribed.value = true
                    mShouldScrollToComment = true
                    mSavedComment = ""
                }
        }
    }

    override fun getPosition(item: Comment): Int {
        val position = super.getPosition(item)
        return if (position == -1) totalSize else position
    }

    override fun getItemId(item: Comment) = item.id

    override fun listItems() = commentService.List()

    override fun getItemList(items: Comments) = items.comments

    override fun getTotalSize(items: Comments) = items.count

    override fun reset() {
        super.reset()
        mSelectedComment.value = null
        mShouldScrollToComment = true
    }

    suspend fun retrieve(postId: ByteString) {
        val newPost = postService.Retrieve().executeFully(Id(id = postId))
        mPost.value = newPost
        mSubscribed.value = newPost.is_subscribed
        acknowledgedPosition = newPost.comments_read - 1
    }

    suspend fun updateSubscription(subscribed: Boolean) {
        postService.UpdateSubscription().executeFully(
            Subscription(
                id = mPost.value.id,
                subscribed = subscribed
            )
        )
        mSubscribed.value = subscribed
    }

    suspend fun report() {
        postService.Report().executeFully(Id(id = mPost.value.id))
    }

    suspend fun delete() {
        postService.Delete().executeFully(Id(id = mPost.value.id))
    }

    suspend fun reportComment(commentId: ByteString) {
        commentService.Report().executeFully(Id(id = commentId))
    }

    suspend fun deleteComment(commentId: ByteString) {
        commentService.Delete().executeFully(Id(id = commentId))
    }

    fun setSelectedComment(position: Int?) {
        mSelectedComment.value = position
        mShouldScrollToComment = position != null
    }

    fun setShouldScrollToComment(shouldScroll: Boolean) {
        mShouldScrollToComment = shouldScroll
    }

    fun acknowledgeComment(position: Int) {
        if (position <= acknowledgedPosition) {
            return
        }

        val comment = items[position] ?: return
        val lastPosition = totalSize - 1
        acknowledgedPosition = position
        em.post(CommentWasSeenEvent(comment, post.value.id, lastPosition - position))
    }

    fun makeDeletedComment(position: Int) = items[position]?.copy(
        is_deleted = true,
        text = ""
    )

    companion object {
        fun provideFactory(assistedFactory: PostViewModelFactory, post: Post) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    assistedFactory.create(post) as T
            }
    }
}
