package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.id
import app.fyreplace.protos.subscription
import com.google.protobuf.ByteString
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
}
