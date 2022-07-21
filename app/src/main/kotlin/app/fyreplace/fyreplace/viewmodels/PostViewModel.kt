package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("CheckResult")
class PostViewModel @AssistedInject constructor(
    @Assisted initialPost: Post,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub,
    private val commentStub: CommentServiceGrpcKt.CommentServiceCoroutineStub
) : ItemRandomAccessListViewModel<Comment, Comments>(initialPost.id) {
    private val mPost = MutableStateFlow(initialPost)
    private val mSubscribed = MutableStateFlow(initialPost.isSubscribed)
    private var mShouldScrollToComment = true
    val post = mPost.asStateFlow()
    val subscribed = mSubscribed.asStateFlow()
    val shouldScrollToComment get() = mShouldScrollToComment

    override fun listItems() = commentStub.list(pages)

    override fun getItemList(items: Comments): List<Comment> = items.commentsList

    override fun getTotalSize(items: Comments) = items.count

    override fun reset() {
        super.reset()
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

    suspend fun createComment(text: String) =
        commentStub.create(commentCreation {
            postId = post.value.id
            this.text = text
        })

    suspend fun reportComment(commentId: ByteString) {
        commentStub.report(id { id = commentId })
    }

    suspend fun deleteComment(position: Int, commentId: ByteString) {
        commentStub.delete(id { id = commentId })
        makeDeletedComment(position)?.let { update(position, it) }
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
