package app.fyreplace.fyreplace.legacy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemListBinding
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.ui.adapters.FeedAdapter
import app.fyreplace.fyreplace.legacy.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.legacy.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.legacy.viewmodels.FeedViewModel
import app.fyreplace.protos.Post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.drop
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment :
    ListFragment(R.layout.fragment_item_list),
    ItemListAdapter.ItemClickListener<Post>,
    FeedAdapter.VoteListener,
    MenuProvider {
    @Inject
    override lateinit var em: EventsManager

    override val rootView get() = if (::bd.isInitialized) bd.root else null
    override val destinationId = R.id.fragment_feed
    override lateinit var bd: FragmentItemListBinding
    override val vm by activityViewModels<FeedViewModel>()
    override val recyclerView get() = bd.recyclerView
    private val cvm by activityViewModels<CentralViewModel>()
    private lateinit var adapter: FeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cvm.isAuthenticated.drop(1).launchCollect {
            when {
                lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) -> refreshListing()
                else -> resetListing()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentItemListBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
        bd.isEmpty = vm.isEmpty
        bd.emptyText = vm.emptyText
        bd.recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
    }

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FeedAdapter(em, viewLifecycleOwner, cvm.isAuthenticated, this, this)
        bd.recyclerView.adapter = adapter
        bd.swipe.setOnRefreshListener { refreshListing() }

        vm.posts.launchCollect(viewLifecycleOwner.lifecycleScope, action = adapter::replaceAll)
    }

    override fun onStart() {
        super.onStart()
        startListing()
    }

    override fun onStop() {
        stopListing()
        super.onStop()
    }

    override fun onItemClick(item: Post, position: Int) {
        val directions = FeedFragmentDirections.toPost(item)
        findNavController().navigate(directions)
    }

    override fun onPostVoted(view: View, position: Int, spread: Boolean) {
        view.provideHapticFeedback(positive = spread)
        launch {
            vm.vote(position, spread)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_feed, menu)
        cvm.isAuthenticated.launchCollect(viewLifecycleOwner.lifecycleScope) {
            menu.findItem(R.id.help)?.isVisible = !it
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.help -> showBasicAlert(R.string.feed_help_title, R.string.feed_help_message)
            else -> return false
        }

        return true
    }

    override fun startListing() {
        vm.startListing().launchCollect(retry = if (retryCount < 3) ::retryListing else null) {
            bd.swipe.isRefreshing = false
            retryCount = 0
        }.invokeOnCompletion { bd.swipe.isRefreshing = false }
    }

    override fun stopListing() = vm.stopListing()

    private fun resetListing() = vm.reset()
}
