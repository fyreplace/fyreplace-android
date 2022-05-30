package app.fyreplace.fyreplace.ui.fragments

import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.ArchiveChangeViewModel
import app.fyreplace.fyreplace.viewmodels.ArchiveViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : ItemListFragment<Post, Posts, ArchiveAdapter.ChapterHolder>(),
    ItemListAdapter.ItemClickListener<Post> {
    override val icvm by sharedViewModel<ArchiveChangeViewModel>()
    override val vm by viewModel<ArchiveViewModel>()
    override val emptyText by lazy { getString(R.string.archive_empty) }

    override fun makeAdapter() = ArchiveAdapter().apply {
        setOnClickListener(this@ArchiveFragment)
    }

    override fun onItemClick(item: Post, position: Int) {
        val directions = ArchiveFragmentDirections.actionPost(post = item.p, position = position)
        findNavController().navigate(directions)
    }
}
