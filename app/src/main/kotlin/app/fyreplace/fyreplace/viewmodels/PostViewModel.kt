package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.stringId
import app.fyreplace.protos.subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("CheckResult")
class PostViewModel(
    initialPost: Post,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub
) : BaseViewModel() {
    private val mPost = MutableStateFlow(initialPost)
    private val mSubscribed = MutableStateFlow(initialPost.isSubscribed)
    val post: Flow<Post> = mPost
    val subscribed: Flow<Boolean> = mSubscribed

    suspend fun retrieve(postId: String) {
        val newPost = postStub.retrieve(stringId { id = postId })
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
        postStub.report(stringId { id = mPost.value.id })
    }

    suspend fun delete() {
        postStub.delete(stringId { id = mPost.value.id })
    }
}
