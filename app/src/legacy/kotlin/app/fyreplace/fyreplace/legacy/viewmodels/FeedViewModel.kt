package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.CommentWasCreatedEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.RemoteNotificationWasReceivedEvent
import app.fyreplace.fyreplace.legacy.extensions.mutateAsList
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceClient
import app.fyreplace.protos.Vote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okio.ByteString
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    override val preferences: SharedPreferences,
    private val em: EventsManager,
    private val postService: PostServiceClient
) : BaseViewModel() {
    private lateinit var votesChannel: SendChannel<Vote>
    private val posts = MutableStateFlow(emptyList<Post>())
    val isEmpty = posts.map(List<Post>::isEmpty).asState(true)
    val emptyText = MutableStateFlow(R.string.feed_empty).asStateFlow()
    private val stalePostIds = mutableSetOf<ByteString>()

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

    fun startListing(): Flow<List<Post>> {
        stalePostIds.addAll(posts.value.map(Post::id))
        val (sender, receiver) = postService.ListFeed().executeFully()
        votesChannel = sender
        return flow {
            for (newPost in receiver) {
                emitFeed(newPost)
            }

            pruneStalePosts()
        }
    }

    fun stopListing() {
        votesChannel.close()
    }

    fun reset() {
        posts.value = emptyList()
        stalePostIds.clear()
    }

    suspend fun vote(position: Int, spread: Boolean) {
        val postId = posts.value[position].id
        votesChannel.send(
            Vote(
                post_id = postId,
                spread = spread
            )
        )
        posts.value = posts.value.filter { it.id != postId }
    }

    private suspend fun FlowCollector<List<Post>>.emitFeed(newPost: Post) {
        val index = posts.value.indexOfFirst { it.id == newPost.id }
        var newFeed: List<Post>

        if (index != -1) {
            newFeed = posts.value.mutateAsList { this[index] = newPost }
            stalePostIds.remove(newPost.id)

            for (id in stalePostIds) {
                if (posts.value.indexOfFirst { it.id == id } < index) {
                    newFeed = newFeed.filter { it.id != id }
                    stalePostIds.remove(id)
                }
            }
        } else {
            newFeed = posts.value + newPost
            pruneStalePosts()
        }

        posts.value = newFeed
        emit(newFeed)
    }

    private fun pruneStalePosts() {
        for (id in stalePostIds) {
            posts.value = posts.value.filter { it.id != id }
        }

        stalePostIds.clear()
    }

    private fun incrementCommentCount(postId: ByteString) {
        posts.value = posts.value.map {
            when (it.id) {
                postId -> it.copy(comment_count = it.comment_count + 1)
                else -> it
            }
        }
    }
}
