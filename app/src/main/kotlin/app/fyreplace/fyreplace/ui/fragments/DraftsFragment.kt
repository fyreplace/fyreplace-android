package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.DraftCreationEvent
import app.fyreplace.fyreplace.events.PostDeletionEvent
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.PrimaryActionProvider
import app.fyreplace.fyreplace.ui.adapters.DraftsAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.ui.adapters.holders.PreviewHolder
import app.fyreplace.fyreplace.viewmodels.DraftsViewModel
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

    override fun makeAdapter() = DraftsAdapter(this)

    override fun onItemClick(item: Post, position: Int) {
        val directions = DraftsFragmentDirections.actionDraft(post = item.p)
        findNavController().navigate(directions)
    }

    override fun onItemLongClick(item: Post, position: Int) =
        showSelectionAlert(null, R.array.drafts_item_choices) { choice ->
            when (choice) {
                0 -> onItemClick(item, position)
                1 -> launch { deletePost(item) }
            }
        }

    override fun getPrimaryActionIcon() = R.drawable.ic_baseline_add

    override fun onPrimaryAction() {
        launch {
            val directions = DraftsFragmentDirections.actionDraft(post = createPost().p)
            findNavController().navigate(directions)
        }
    }

    private suspend fun createPost(): Post {
        val post = post { id = vm.create().id }
        vm.em.post(DraftCreationEvent(post))
        return post
    }

    private suspend fun deletePost(post: Post) {
        vm.delete(post.id)
        vm.em.post(PostDeletionEvent(post))
    }
}
