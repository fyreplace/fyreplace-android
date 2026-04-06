package app.fyreplace.fyreplace.legacy.ui.fragments

import android.view.View
import androidx.core.view.doOnLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.ArchivePagesBinding
import app.fyreplace.fyreplace.legacy.ui.CustomTitleProvider
import app.fyreplace.fyreplace.legacy.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.legacy.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.legacy.ui.adapters.holders.PreviewHolder
import app.fyreplace.fyreplace.legacy.viewmodels.ArchiveViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ArchiveFragment :
    ItemListFragment<Post, Posts, PreviewHolder>(),
    ItemListAdapter.ItemClickListener<Post>,
    CustomTitleProvider {
    override val destinationId = R.id.fragment_archive
    override val vm by activityViewModels<ArchiveViewModel>()
    override val recyclerView get() = bd.recyclerView

    override fun makeAdapter() = ArchiveAdapter(this)

    override fun onItemClick(item: Post, position: Int) {
        val directions = ArchiveFragmentDirections.toPost(post = item)
        findNavController().navigate(directions)
    }

    override fun getCustomTitleView() = ArchivePagesBinding.inflate(layoutInflater).run {
        lifecycleOwner = viewLifecycleOwner
        ui = this@ArchiveFragment
        pages.doOnLayout {
            launch {
                delay(100)
                pages.check(vm.selectedPage.value)
            }
        }
        return@run root
    }

    fun onAllPostsClicked(view: View) = refreshListing { vm.selectPage(view.id) }

    fun onOwnPostsClicked(view: View) = refreshListing { vm.selectPage(view.id) }
}
