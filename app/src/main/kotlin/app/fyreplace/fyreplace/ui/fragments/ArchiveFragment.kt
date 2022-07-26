package app.fyreplace.fyreplace.ui.fragments

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.ArchiveChangeViewModel
import app.fyreplace.fyreplace.viewmodels.ArchiveViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchiveFragment :
    ItemListFragment<Post, Posts, ArchiveAdapter.ChapterHolder>(),
    ItemListAdapter.ItemClickListener<Post> {
    override val icvm by activityViewModels<ArchiveChangeViewModel>()
    override val vm by viewModels<ArchiveViewModel>()
    override val emptyText by lazy { getString(R.string.archive_empty) }

    override fun makeAdapter() = ArchiveAdapter(this)

    override fun onItemClick(item: Post, position: Int) {
        val directions = ArchiveFragmentDirections.actionPost(post = item.p, position = position)
        findNavController().navigate(directions)
    }
}
