package app.fyreplace.fyreplace.viewmodels

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.CommentWasCreatedEvent
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.events.RemoteNotificationWasReceivedEvent
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.Vote
import app.fyreplace.protos.vote
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val em: EventsManager,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub
) :
    BaseViewModel() {
    private val maybeVotes = MutableSharedFlow<Vote?>(replay = 10)
    private val votes get() = maybeVotes.takeWhile { it != null }.filterNotNull()
    private val mPosts = MutableStateFlow(emptyList<Post>())
    val posts = mPosts.asStateFlow()
    val isEmpty = posts.map { it.isEmpty() }.asState(true)
    val emptyText = MutableStateFlow(R.string.feed_empty).asStateFlow()

    init {
        viewModelScope.launch {
            em.events.filterIsInstance<CommentWasCreatedEvent>()
                .collect { incrementCommentCount(it.postId) }
        }

        viewModelScope.launch {
            em.events.filterIsInstance<RemoteNotificationWasReceivedEvent>()
                .filter { it.command == "comment:creation" }
                .collect { incrementCommentCount(it.postId) }
        }
    }

    fun startListing() = postStub.listFeed(votes)
        .onEach { post ->
            val index = posts.value.indexOfFirst { it.id == post.id }

            mPosts.value =
                (if (index >= 0) posts.value.mapIndexed { i, p -> if (i == index) post else p }
                else posts.value + post)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stopListing() {
        maybeVotes.tryEmit(null)
        maybeVotes.resetReplayCache()
    }

    fun reset() {
        mPosts.value = emptyList()
    }

    suspend fun vote(position: Int, spread: Boolean) {
        val postId = posts.value[position].id
        maybeVotes.emit(vote {
            this.postId = postId
            this.spread = spread
        })
        mPosts.value = mPosts.value.filter { it.id != postId }
    }

    private fun incrementCommentCount(postId: ByteString) {
        mPosts.value = posts.value.map {
            if (it.id == postId) Post.newBuilder(it)
                .setCommentCount(it.commentCount + 1)
                .build()
            else it
        }
    }
}
