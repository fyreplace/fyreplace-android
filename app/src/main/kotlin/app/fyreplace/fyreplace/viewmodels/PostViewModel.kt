package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.fyreplace.events.CommentCreationEvent
import app.fyreplace.fyreplace.events.CommentDeletionEvent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*

@SuppressLint("CheckResult")
class PostViewModel @AssistedInject constructor(
    em: EventsManager,
    @Assisted initialPost: Post,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub,
    private val commentStub: CommentServiceGrpcKt.CommentServiceCoroutineStub
) : ItemRandomAccessListViewModel<Comment, Comments>(em, initialPost.id) {
    override val addedItems = em.events.filterIsInstance<CommentCreationEvent>()
        .filter { it.postId == post.value.id }
        .map { it.atPosition(totalSize.value) }
    override val updatedItems = em.events.filterIsInstance<CommentDeletionEvent>()
        .filter { it.postId == post.value.id }
    private val mPost = MutableStateFlow(initialPost)
    private val mSubscribed = MutableStateFlow(initialPost.isSubscribed)
    private var mSelectedComment = MutableStateFlow<Int?>(null)
    private var mShouldScrollToComment = true
    val post = mPost.asStateFlow()
    val subscribed = mSubscribed.asStateFlow()
    val selectedComment = mSelectedComment.asStateFlow()
    val shouldScrollToComment get() = mShouldScrollToComment

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

    suspend fun createComment(text: String) = commentStub.create(commentCreation {
        postId = post.value.id
        this.text = text
    })

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

    fun setScrolledToComment() {
        mShouldScrollToComment = false
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
