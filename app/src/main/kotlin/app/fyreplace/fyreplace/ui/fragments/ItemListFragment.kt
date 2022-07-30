package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemListBinding
import app.fyreplace.fyreplace.ui.adapters.ItemHolder
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.EventsViewModel
import app.fyreplace.fyreplace.viewmodels.ItemListViewModel
import app.fyreplace.fyreplace.viewmodels.events.ItemPositionalEvent
import app.fyreplace.fyreplace.viewmodels.events.PositionalEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

abstract class ItemListFragment<Item, Items, VH : ItemHolder> :
    BaseFragment(R.layout.fragment_item_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView by lazy { if (::bd.isInitialized) bd.root else null }
    protected abstract val vm: ItemListViewModel<Item, Items>
    protected abstract val addedItems: Flow<ItemPositionalEvent<Item>>
    protected abstract val updatedItems: Flow<ItemPositionalEvent<Item>>
    protected abstract val removedPositions: Flow<PositionalEvent>
    protected val evm by activityViewModels<EventsViewModel>()
    protected lateinit var bd: FragmentItemListBinding
    private val vmEventJobs = mutableListOf<Job>()
    private val adapterEventJobs = mutableListOf<Job>()
    private lateinit var adapter: ItemListAdapter<Item, VH>

    abstract fun makeAdapter(): ItemListAdapter<Item, VH>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshViewModelEventsHandlers()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.reset()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postponeEnterTransition()
        val view = super.onCreateView(inflater, container, savedInstanceState)?.also {
            it.doOnPreDraw { startPostponedEnterTransition() }
            bd = FragmentItemListBinding.bind(it)
        }
        bd.lifecycleOwner = viewLifecycleOwner
        bd.isEmpty = vm.isEmpty
        bd.emptyText = vm.emptyText

        with(bd.recyclerView) {
            setHasFixedSize(true)
            addOnChildAttachStateChangeListener(this@ItemListFragment)
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = makeAdapter()
        adapter.addAll(vm.items)
        bd.recyclerView.adapter = adapter
        bd.swipe.setOnRefreshListener {
            reset()
            launch { vm.fetchMore() }
        }

        refreshAdapterEventsHandlers()
    }

    override fun onDestroyView() {
        bd.recyclerView.removeOnChildAttachStateChangeListener(this)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        startListing()
    }

    override fun onStop() {
        stopListing()
        super.onStop()
    }

    override fun onChildViewAttachedToWindow(view: View) {
        val childPosition = bd.recyclerView.getChildAdapterPosition(view)

        if (adapter.itemCount - childPosition < ItemListViewModel.PAGE_SIZE) launch {
            vm.fetchMore()
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit

    protected fun startListing() {
        launch {
            vm.startListing().launchCollect {
                bd.swipe.isRefreshing = false
                adapter.addAll(it)
            }

            if (adapter.itemCount == 0) {
                vm.fetchMore()
            }
        }
    }

    protected fun stopListing() = vm.stopListing()

    protected fun reset() {
        adapter.removeAll()
        vm.reset()
    }

    protected fun refreshViewModelEventsHandlers() {
        vmEventJobs.forEach { it.cancel() }
        vmEventJobs.clear()

        vmEventJobs.add(addedItems.launchCollect {
            vm.add(it.position, it.item)
        })
        vmEventJobs.add(updatedItems.launchCollect {
            vm.update(it.position, it.item)
        })
        vmEventJobs.add(removedPositions.launchCollect {
            vm.remove(it.position)
        })
    }

    protected fun refreshAdapterEventsHandlers() {
        adapterEventJobs.forEach { it.cancel() }
        adapterEventJobs.clear()

        adapterEventJobs.add(addedItems.launchCollect(viewLifecycleOwner.lifecycleScope) {
            adapter.add(it.position, it.item)
        })
        adapterEventJobs.add(updatedItems.launchCollect(viewLifecycleOwner.lifecycleScope) {
            adapter.update(it.position, it.item)
        })
        adapterEventJobs.add(removedPositions.launchCollect(viewLifecycleOwner.lifecycleScope) {
            adapter.remove(it.position)
        })
    }
}
