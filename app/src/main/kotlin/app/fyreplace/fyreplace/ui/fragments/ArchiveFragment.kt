package app.fyreplace.fyreplace.ui.fragments

import android.content.Context
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.viewmodels.ArchiveDeletionViewModel
import app.fyreplace.fyreplace.viewmodels.ArchiveViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : ItemListFragment<Post, Posts>() {
    override val idvm by sharedViewModel<ArchiveDeletionViewModel>()
    override val vm by viewModel<ArchiveViewModel>()
    override val emptyText by lazy { getString(R.string.archive_empty) }

    override fun makeAdapter(context: Context) = ArchiveAdapter(context).apply {
        setOnClickListener { post, position ->
            val directions = ArchiveFragmentDirections.actionPost(post = post, position = position)
            findNavController().navigate(directions)
        }
    }
}
