package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.SharedPreferences
import androidx.annotation.IdRes
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.DraftWasPublishedEvent
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.ItemEvent
import app.fyreplace.fyreplace.legacy.events.PostWasDeletedEvent
import app.fyreplace.fyreplace.legacy.events.PostWasSeenEvent
import app.fyreplace.fyreplace.legacy.events.PostWasSubscribedToEvent
import app.fyreplace.fyreplace.legacy.events.PostWasUnsubscribedFromEvent
import app.fyreplace.protos.Post
import app.fyreplace.protos.PostServiceClient
import app.fyreplace.protos.Posts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    override val preferences: SharedPreferences,
    em: EventsManager,
    private val postService: PostServiceClient
) : ItemListViewModel<Post, Posts>(em) {
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
                    val position = getPosition(it.item)

                    if (position != -1) {
                        onItemRemoved(it.at(position))
                    }

                    onItemAdded(it.at(0))
                }
        }
    }

    override fun getItemId(item: Post) = item.id

    override fun listItems() =
        if (selectedPage.value == R.id.all_posts) postService.ListArchive()
        else postService.ListOwnPosts()

    override fun getNextCursor(items: Posts) = items.next

    override fun getItemList(items: Posts) = items.posts

    fun selectPage(@IdRes index: Int) {
        mSelectedPage.value = index
    }
}
