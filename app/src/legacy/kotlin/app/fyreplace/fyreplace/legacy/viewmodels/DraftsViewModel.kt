package app.fyreplace.fyreplace.legacy.viewmodels

import android.annotation.SuppressLint
import android.content.SharedPreferences
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.DraftWasCreatedEvent
import app.fyreplace.fyreplace.legacy.events.DraftWasDeletedEvent
import app.fyreplace.fyreplace.legacy.events.DraftWasPublishedEvent
import app.fyreplace.fyreplace.legacy.events.DraftWasUpdatedEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.protos.Id
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceClient
import app.fyreplace.protos.Posts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.merge
import okio.ByteString
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class DraftsViewModel @Inject constructor(
    override val preferences: SharedPreferences,
    em: EventsManager,
    private val postService: PostServiceClient
) :
    ItemListViewModel<Post, Posts>(em) {
    override val addedItems = em.events.filterIsInstance<DraftWasCreatedEvent>()
    override val updatedItems = em.events.filterIsInstance<DraftWasUpdatedEvent>()
    override val removedItems = merge(
        em.events.filterIsInstance<DraftWasDeletedEvent>(),
        em.events.filterIsInstance<DraftWasPublishedEvent>()
    )
    override val emptyText = MutableStateFlow(R.string.drafts_empty).asStateFlow()

    override fun getItemId(item: Post) = item.id

    override fun listItems() = postService.ListDrafts()

    override fun getNextCursor(items: Posts) = items.next

    override fun getItemList(items: Posts) = items.posts

    suspend fun create() = postService.Create().executeFully(Unit)

    suspend fun delete(postId: ByteString) = postService.Delete().executeFully(Id(id = postId))
}
