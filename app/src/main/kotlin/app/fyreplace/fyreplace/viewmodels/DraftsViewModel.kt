package app.fyreplace.fyreplace.viewmodels

import android.annotation.SuppressLint
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.*
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import com.google.protobuf.Empty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
@SuppressLint("CheckResult")
class DraftsViewModel @Inject constructor(
    em: EventsManager,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub
) :
    ItemListViewModel<Post, Posts>(em) {
    override val addedItems = em.events.filterIsInstance<DraftWasCreatedEvent>()
    override val updatedItems = em.events.filterIsInstance<DraftWasUpdatedEvent>()
    override val removedItems = merge(
        em.events.filterIsInstance<DraftWasDeletedEvent>(),
        em.events.filterIsInstance<DraftWasPublishedEvent>()
    )
    override val emptyText = MutableStateFlow(R.string.drafts_empty).asStateFlow()

    override fun getItemId(item: Post): ByteString = item.id

    override fun listItems() = postStub.listDrafts(pages)

    override fun hasNextCursor(items: Posts) = items.hasNext()

    override fun getNextCursor(items: Posts): Cursor = items.next

    override fun getItemList(items: Posts): List<Post> = items.postsList

    suspend fun create() = postStub.create(Empty.getDefaultInstance())

    suspend fun delete(postId: ByteString) {
        postStub.delete(id { id = postId })
    }
}
