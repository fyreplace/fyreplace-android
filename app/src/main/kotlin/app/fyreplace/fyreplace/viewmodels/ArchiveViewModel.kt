package app.fyreplace.fyreplace.viewmodels

import androidx.annotation.IdRes
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.*
import app.fyreplace.protos.Cursor
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceGrpcKt
import app.fyreplace.protos.Posts
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    em: EventsManager,
    private val postStub: PostServiceGrpcKt.PostServiceCoroutineStub
) :
    ItemListViewModel<Post, Posts>(em) {
    private val mSelectedPage = MutableStateFlow(R.id.all_posts)
    val selectedPage = mSelectedPage.asStateFlow()
    override val addedItems = merge(
        em.events.filterIsInstance<DraftWasPublishedEvent>(),
        selectedPage.filter { it == R.id.all_posts }
            .flatMapConcat { em.events.filterIsInstance<PostWasSubscribedToEvent>() }
    )
    override val updatedItems = emptyFlow<ItemEvent<Post>>()
    override val removedItems = merge(
        em.events.filterIsInstance<PostWasDeletedEvent>(),
        selectedPage.filter { it == R.id.all_posts }
            .flatMapConcat { em.events.filterIsInstance<PostWasUnsubscribedFromEvent>() }
    )
    override val emptyText = selectedPage
        .map { if (it == R.id.all_posts) R.string.archive_all_empty else R.string.archive_own_empty }
        .asState(R.string.archive_all_empty)

    init {
        viewModelScope.launch {
            em.events.filterIsInstance<PostWasSeenEvent>()
                .collect {
                    onItemRemoved(it.at(getPosition(it.item)))
                    onItemAdded(it.at(0))
                }
        }
    }

    override fun getItemId(item: Post): ByteString = item.id

    override fun listItems() =
        if (selectedPage.value == R.id.all_posts) postStub.listArchive(pages)
        else postStub.listOwnPosts(pages)

    override fun hasNextCursor(items: Posts) = items.hasNext()

    override fun getNextCursor(items: Posts): Cursor = items.next

    override fun getItemList(items: Posts): List<Post> = items.postsList

    fun selectPage(@IdRes index: Int) {
        mSelectedPage.value = index
    }
}
