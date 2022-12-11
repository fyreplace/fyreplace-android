package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.fyreplace.R
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.Vote
import app.fyreplace.protos.vote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub) :
    BaseViewModel() {
    private val maybeVotes = MutableSharedFlow<Vote?>(replay = 10)
    private val votes get() = maybeVotes.takeWhile { it != null }.filterNotNull()
    private val mPosts = MutableStateFlow(emptyList<Post>())
    val posts = mPosts.asStateFlow()
    val isEmpty = posts.map { it.isEmpty() }.asState(true)
    val emptyText = MutableStateFlow(R.string.feed_empty).asStateFlow()

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
}
