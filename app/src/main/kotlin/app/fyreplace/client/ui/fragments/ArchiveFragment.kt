package app.fyreplace.client.ui.fragments

import android.content.Context
import androidx.navigation.fragment.findNavController
import app.fyreplace.client.R
import app.fyreplace.client.ui.adapters.ArchiveAdapter
import app.fyreplace.client.viewmodels.ArchiveViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : ItemListFragment<Post, Posts>() {
    override val vm by viewModel<ArchiveViewModel>()
    override val emptyText get() = requireContext().getString(R.string.archive_empty)

    override fun makeAdapter(context: Context) = ArchiveAdapter(context).apply {
        setOnClickListener { post, position ->
            val directions = ArchiveFragmentDirections.actionPost(
                deletionNotifier = ItemDeletionNotifier(position),
                post = post
            )
            findNavController().navigate(directions)
        }
    }
}
