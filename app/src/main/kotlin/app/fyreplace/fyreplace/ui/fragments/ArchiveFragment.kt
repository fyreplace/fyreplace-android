package app.fyreplace.fyreplace.ui.fragments

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ArchivePagesBinding
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.CustomTitleProvider
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
    CustomTitleProvider,
    ItemListAdapter.ItemClickListener<Post> {
    override val icvm by activityViewModels<ArchiveChangeViewModel>()
    override val vm by viewModels<ArchiveViewModel>()
    override val emptyText by lazy { getString(R.string.archive_empty) }

    override fun makeAdapter() = ArchiveAdapter(this)

    override fun getCustomTitleView() = ArchivePagesBinding.inflate(layoutInflater).run {
        lifecycleOwner = viewLifecycleOwner
        ui = this@ArchiveFragment
        pages.check(vm.selectedPage)
        return@run root
    }

    override fun onItemClick(item: Post, position: Int) {
        val directions = ArchiveFragmentDirections.actionPost(post = item.p, position = position)
        findNavController().navigate(directions)
    }

    fun onAllPostsClicked(view: View) = selectPage(view.id)

    fun onOwnPostsClicked(view: View) = selectPage(view.id)

    private fun selectPage(@IdRes page: Int) {
        stopListing()
        vm.selectPage(page)
        reset()
        startListing()
    }
}
