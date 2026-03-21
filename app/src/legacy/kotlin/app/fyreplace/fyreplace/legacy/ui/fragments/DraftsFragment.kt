package app.fyreplace.fyreplace.legacy.ui.fragments

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.events.DraftWasCreatedEvent
import app.fyreplace.fyreplace.legacy.events.DraftWasDeletedEvent
import app.fyreplace.fyreplace.legacy.grpc.p
import app.fyreplace.fyreplace.legacy.ui.PrimaryActionProvider
import app.fyreplace.fyreplace.legacy.ui.adapters.DraftsAdapter
import app.fyreplace.fyreplace.legacy.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.legacy.ui.adapters.holders.PreviewHolder
import app.fyreplace.fyreplace.legacy.viewmodels.DraftsViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import app.fyreplace.protos.post
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DraftsFragment :
    ItemListFragment<Post, Posts, PreviewHolder>(),
    ItemListAdapter.ItemClickListener<Post>,
    PrimaryActionProvider {
    override val vm by activityViewModels<DraftsViewModel>()
    override val primaryActionIcon = R.drawable.ic_baseline_add

    override fun makeAdapter() = DraftsAdapter(this)

    override fun onItemClick(item: Post, position: Int) {
        val directions = DraftsFragmentDirections.toDraft(post = item.p)
        findNavController().navigate(directions)
    }

    override fun onItemLongClick(item: Post, position: Int) =
        showSelectionAlert(null, R.array.drafts_item_choices) { choice ->
            when (choice) {
                0 -> onItemClick(item, position)
                1 -> launch { deletePost(item) }
            }
        }

    override fun onPrimaryAction() {
        launch {
            val directions = DraftsFragmentDirections.toDraft(post = createPost().p)
            findNavController().navigate(directions)
        }
    }

    private suspend fun createPost(): Post {
        val post = post { id = vm.create().id }
        vm.em.post(DraftWasCreatedEvent(post))
        return post
    }

    private suspend fun deletePost(post: Post) {
        vm.delete(post.id)
        vm.em.post(DraftWasDeletedEvent(post))
    }
}
