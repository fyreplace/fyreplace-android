package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.events.*
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlin.math.min

@SuppressLint("CheckResult")
class PostViewModel @AssistedInject constructor(
    em: EventsManager,
    @Assisted initialPost: Post,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub,
    private val commentStub: CommentServiceGrpcKt.CommentServiceCoroutineStub
) : ItemRandomAccessListViewModel<Comment, Comments>(em, initialPost.id) {
    override val addedItems = em.events.filterIsInstance<CommentWasCreatedEvent>()
        .filter { it.postId == post.value.id }
    override val updatedItems = em.events.filterIsInstance<CommentWasDeletedEvent>()
        .filter { it.postId == post.value.id }
    private val mPost = MutableStateFlow(initialPost)
    private val mSubscribed = MutableStateFlow(initialPost.isSubscribed)
    private var mSelectedComment = MutableStateFlow<Int?>(null)
    private var mShouldScrollToComment = true
    private var mSavedComment = ""
    private var acknowledgedPosition = -1
    val post = mPost.asStateFlow()
    val subscribed = mSubscribed.asStateFlow()
    val selectedComment = mSelectedComment.asStateFlow()
    val shouldScrollToComment get() = mShouldScrollToComment
    val scrollTargetPosition
        get() = selectedComment.value ?: min(post.value.commentsRead, totalSize)
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
                    mPost.value = Post.newBuilder(post.value)
                        .setCommentsRead(post.value.commentsRead + 1)
                        .build()
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

    override fun getItemId(item: Comment): ByteString = item.id

    override fun listItems() = commentStub.list(pages)

    override fun getItemList(items: Comments): List<Comment> = items.commentsList

    override fun getTotalSize(items: Comments) = items.count

    override fun reset() {
        super.reset()
        mSelectedComment.value = null
        mShouldScrollToComment = true
    }

    suspend fun retrieve(postId: ByteString) {
        val newPost = postStub.retrieve(id { id = postId })
        mPost.value = newPost
        mSubscribed.value = newPost.isSubscribed
        acknowledgedPosition = newPost.commentsRead - 1
    }

    suspend fun updateSubscription(subscribed: Boolean) {
        postStub.updateSubscription(subscription {
            id = mPost.value.id
            this.subscribed = subscribed
        })
        mSubscribed.value = subscribed
    }

    suspend fun report() {
        postStub.report(id { id = mPost.value.id })
    }

    suspend fun delete() {
        postStub.delete(id { id = mPost.value.id })
    }

    suspend fun reportComment(commentId: ByteString) {
        commentStub.report(id { id = commentId })
    }

    suspend fun deleteComment(commentId: ByteString) {
        commentStub.delete(id { id = commentId })
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

    fun makeDeletedComment(position: Int) = items[position]?.copy {
        isDeleted = true
        text = ""
    }

    companion object {
        fun provideFactory(
            assistedFactory: PostViewModelFactory,
            post: Post
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                assistedFactory.create(post) as T
        }
    }
}