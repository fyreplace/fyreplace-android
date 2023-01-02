package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemListBinding
import app.fyreplace.fyreplace.events.EventsManager
import app.fyreplace.fyreplace.grpc.p
import app.fyreplace.fyreplace.ui.adapters.FeedAdapter
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.CentralViewModel
import app.fyreplace.fyreplace.viewmodels.FeedViewModel
import app.fyreplace.protos.Post
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment :
    BaseFragment(R.layout.fragment_item_list),
    ItemListAdapter.ItemClickListener<Post>,
    FeedAdapter.VoteListener,
    MenuProvider {
    @Inject
    lateinit var em: EventsManager

    override val rootView get() = if (::bd.isInitialized) bd.root else null
    override val vm by activityViewModels<FeedViewModel>()
    private val cvm by activityViewModels<CentralViewModel>()
    private lateinit var bd: FragmentItemListBinding
    private lateinit var adapter: FeedAdapter
    private var canAutoRefresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cvm.isAuthenticated.launchCollect {
            when {
                !canAutoRefresh -> return@launchCollect
                lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) -> refreshListing()
                else -> reset()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FeedAdapter(em, viewLifecycleOwner, cvm.isAuthenticated, this, this)
        adapter.addAll(vm.posts.value)
        bd.recyclerView.adapter = adapter
        bd.swipe.setOnRefreshListener { refreshListing() }
        canAutoRefresh = true
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
        val directions = FeedFragmentDirections.actionPost(item.p)
        findNavController().navigate(directions)
    }

    override fun onPostVoted(view: View, position: Int, spread: Boolean) {
        launch {
            vm.vote(position, spread)
            adapter.remove(position)
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

    private fun startListing() {
        vm.startListing().launchCollect {
            bd.swipe.isRefreshing = false
            adapter.addOrUpdate(it)
        }.invokeOnCompletion { bd.swipe.isRefreshing = false }
    }

    private fun stopListing() = vm.stopListing()

    private fun refreshListing() {
        stopListing()
        reset()
        startListing()
    }

    private fun reset() {
        adapter.removeAll()
        vm.reset()
    }
}
