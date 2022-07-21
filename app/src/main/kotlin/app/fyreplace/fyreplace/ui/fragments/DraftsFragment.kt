package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.PrimaryActionProvider
import app.fyreplace.fyreplace.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.ui.adapters.DraftsAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.DraftsChangeViewModel
import app.fyreplace.fyreplace.viewmodels.DraftsViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import app.fyreplace.protos.post
import com.google.protobuf.ByteString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DraftsFragment :
    ItemListFragment<Post, Posts, ArchiveAdapter.ChapterHolder>(),
    PrimaryActionProvider,
    ItemListAdapter.ItemClickListener<Post> {
    override val icvm by activityViewModels<DraftsChangeViewModel>()
    override val vm by viewModels<DraftsViewModel>()
    override val emptyText by lazy { getString(R.string.drafts_empty) }

    override fun makeAdapter() = DraftsAdapter().apply {
        setOnClickListener(this@DraftsFragment)
    }

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
        icvm.add(0, post)
        return post
    }

    private suspend fun deletePost(postId: ByteString, position: Int) {
        vm.delete(postId)
        icvm.delete(position)
    }
}
