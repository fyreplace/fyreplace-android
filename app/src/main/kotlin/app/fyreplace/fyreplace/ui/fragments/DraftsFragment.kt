package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.MainActivity
import app.fyreplace.fyreplace.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.ui.adapters.DraftsAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.DraftsChangeViewModel
import app.fyreplace.fyreplace.viewmodels.DraftsViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import app.fyreplace.protos.post
import com.google.protobuf.ByteString
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DraftsFragment : ItemListFragment<Post, Posts, ArchiveAdapter.ChapterHolder>(),
    ItemListAdapter.ItemClickListener<Post> {
    override val icvm by sharedViewModel<DraftsChangeViewModel>()
    override val vm by viewModel<DraftsViewModel>()
    override val emptyText by lazy { getString(R.string.drafts_empty) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setPrimaryAction(R.drawable.ic_baseline_add) {
            launch {
                val directions = DraftsFragmentDirections.actionDraft(
                    post = createPost().p,
                    position = 0
                )
                findNavController().navigate(directions)
            }
        }
    }

    override fun makeAdapter() = DraftsAdapter().apply {
        setOnClickListener(this@DraftsFragment)
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
