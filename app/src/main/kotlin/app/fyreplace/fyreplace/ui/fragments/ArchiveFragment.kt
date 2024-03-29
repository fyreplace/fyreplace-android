package app.fyreplace.fyreplace.ui.fragments

import android.view.View
import androidx.core.view.doOnLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.fyreplace.fyreplace.databinding.ArchivePagesBinding
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.CustomTitleProvider
import app.fyreplace.fyreplace.ui.adapters.ArchiveAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.ui.adapters.holders.PreviewHolder
import app.fyreplace.fyreplace.viewmodels.ArchiveViewModel
import app.fyreplace.protos.Post
import app.fyreplace.protos.Posts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class ArchiveFragment :
    ItemListFragment<Post, Posts, PreviewHolder>(),
    ItemListAdapter.ItemClickListener<Post>,
    CustomTitleProvider {
    override val vm by activityViewModels<ArchiveViewModel>()

    override fun makeAdapter() = ArchiveAdapter(this)

    override fun onItemClick(item: Post, position: Int) {
        val directions = ArchiveFragmentDirections.actionPost(post = item.p)
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
