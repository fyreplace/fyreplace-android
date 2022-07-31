package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.PrimaryActionProvider
import app.fyreplace.fyreplace.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.ui.adapters.DraftsAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.DraftsViewModel
import app.fyreplace.fyreplace.viewmodels.events.*
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import app.fyreplace.protos.post
import com.google.protobuf.ByteString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

@AndroidEntryPoint
class DraftsFragment :
    ItemListFragment<Post, Posts, ArchiveAdapter.ChapterHolder>(),
    PrimaryActionProvider,
    ItemListAdapter.ItemClickListener<Post> {
    override val vm by viewModels<DraftsViewModel>()
    override val addedItems: Flow<ItemPositionalEvent<Post>>
        get() = evm.events.filterIsInstance<DraftCreationEvent>()
            .map { it.atPosition(0) }
    override val updatedItems: Flow<ItemPositionalEvent<Post>>
        get() = evm.events.filterIsInstance<DraftUpdateEvent>()
    override val removedItems: Flow<PositionalEvent>
        get() = merge(
            evm.events.filterIsInstance<DraftDeletionEvent>(),
            evm.events.filterIsInstance<DraftPublicationEvent>()
        )

    override fun makeAdapter() = DraftsAdapter(this)

    override fun getPrimaryActionIcon() = R.drawable.ic_baseline_add

    override fun onPrimaryAction() {
        launch {
            val directions = DraftsFragmentDirections.actionDraft(
                post = createPost().p,
                position = 0
            )
            findNavController().navigate(directions)
        }
    }

    override fun onItemClick(item: Post, position: Int) {
        val directions = DraftsFragmentDirections.actionDraft(post = item.p, position = position)
        findNavController().navigate(directions)
    }

    override fun onItemLongClick(item: Post, position: Int) {
        showSelectionAlert(null, R.array.drafts_item_choices) { choice ->
            when (choice) {
                0 -> onItemClick(item, position)
                1 -> launch { deletePost(item.id, position) }
            }
        }
    }

    private suspend fun createPost(): Post {
        val post = post { id = vm.create().id }
        evm.post(DraftCreationEvent(post))
        return post
    }

    private suspend fun deletePost(postId: ByteString, position: Int) {
        vm.delete(postId)
        evm.post(PostDeletionEvent(position))
    }
}
