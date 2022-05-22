package app.fyreplace.fyreplace.ui.fragments

import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.ui.adapters.DraftsAdapter
import app.fyreplace.fyreplace.viewmodels.DraftsChangeViewModel
import app.fyreplace.fyreplace.viewmodels.DraftsViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DraftsFragment : ItemListFragment<Post, Posts, ArchiveAdapter.ChapterHolder>() {
    override val icvm by sharedViewModel<DraftsChangeViewModel>()
    override val vm by viewModel<DraftsViewModel>()
    override val emptyText by lazy { getString(R.string.drafts_empty) }

    override fun makeAdapter() = DraftsAdapter()
}
