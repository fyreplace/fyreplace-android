package app.fyreplace.fyreplace.viewmodels

import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.*
import app.fyreplace.protos.*
import com.google.protobuf.ByteString
import com.google.protobuf.Empty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class DraftsViewModel @Inject constructor(
    em: EventsManager,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub
) :
    ItemListViewModel<Post, Posts>(em) {
    override val addedItems = em.events.filterIsInstance<DraftCreationEvent>()
    override val updatedItems = em.events.filterIsInstance<DraftUpdateEvent>()
    override val removedItems = merge(
        em.events.filterIsInstance<DraftDeletionEvent>(),
        em.events.filterIsInstance<DraftPublicationEvent>()
    )
    override val emptyText = emptyFlow<Int>().asState(R.string.drafts_empty)

    override fun getItemId(item: Post): ByteString = item.id

    override fun listItems() = postStub.listDrafts(pages)

    override fun hasNextCursor(items: Posts) = items.hasNext()

    override fun getNextCursor(items: Posts): Cursor = items.next

    override fun getItemList(items: Posts): List<Post> = items.postsList

    suspend fun create() = postStub.create(Empty.getDefaultInstance())

    suspend fun delete(postId: ByteString) = postStub.delete(id { id = postId })
}
