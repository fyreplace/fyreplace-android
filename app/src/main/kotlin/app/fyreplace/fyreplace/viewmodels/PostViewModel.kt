package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.fyreplace.events.CommentWasCreatedEvent
import app.fyreplace.fyreplace.events.CommentWasDeletedEvent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance

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
    val post = mPost.asStateFlow()
    val subscribed = mSubscribed.asStateFlow()
    val selectedComment = mSelectedComment.asStateFlow()
    val shouldScrollToComment get() = mShouldScrollToComment

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

    suspend fun createComment(text: String): Id {
        val id = commentStub.create(commentCreation {
            postId = post.value.id
            this.text = text
        })
        mSubscribed.value = true
        return id
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
