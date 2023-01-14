package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemRandomAccessListBinding
import app.fyreplace.fyreplace.events.NetworkConnectionWasChangedEvent
import app.fyreplace.fyreplace.events.PositionalEvent
import app.fyreplace.fyreplace.ui.adapters.ItemRandomAccessListAdapter
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.viewmodels.ItemRandomAccessListViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance

abstract class ItemRandomAccessListFragment<Item, Items, VH : ItemHolder> :
    ScrollingListFragment<Item>(R.layout.fragment_item_random_access_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView get() = if (::bd.isInitialized) bd.root else null
    abstract override val vm: ItemRandomAccessListViewModel<Item, Items>
    protected lateinit var bd: FragmentItemRandomAccessListBinding
    protected lateinit var adapter: ItemRandomAccessListAdapter<Item, VH>
    private var retryCount = 0

    abstract fun makeAdapter(): ItemRandomAccessListAdapter<Item, VH>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = makeAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postponeEnterTransition()
        val view = super.onCreateView(inflater, container, savedInstanceState)?.also {
            it.doOnPreDraw { startPostponedEnterTransition() }
            bd = FragmentItemRandomAccessListBinding.bind(it)
        }
        bd.lifecycleOwner = viewLifecycleOwner

        with(bd.recyclerView) {
            setHasFixedSize(true)
            addOnChildAttachStateChangeListener(this@ItemRandomAccessListFragment)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        return view
    }

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.resetTo(vm.items, vm.totalSize)
        bd.recyclerView.adapter = adapter

        vm.em.events.filterIsInstance<NetworkConnectionWasChangedEvent>()
            .debounce(1000)
            .launchCollect(viewLifecycleOwner.lifecycleScope) { retryListing() }
    }

    override fun onDestroyView() {
        bd.recyclerView.removeOnChildAttachStateChangeListener(this)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        retryCount = 0
        launch { startFetchingData() }
    }

    override fun onStop() {
        super.onStop()
        vm.stopListing()
    }

    override fun addItem(event: PositionalEvent<Item>) =
        adapter.insert(event.event.item)

    override fun updateItem(event: PositionalEvent<Item>) =
        adapter.update(event.position, event.event.item)

    override fun removeItem(event: PositionalEvent<Item>) = Unit

    override fun onChildViewAttachedToWindow(view: View) {
        val itemPosition = bd.recyclerView.getChildAdapterPosition(view) - 1

        if (vm.items[itemPosition] == null) launch {
            vm.fetchAround(itemPosition)
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit

    open fun onFetchedItems(position: Int, items: List<Item>) {
        adapter.update(position, items)

        if (adapter.totalSize == 0) {
            adapter.setTotalSize(vm.totalSize)
        }
    }

    open suspend fun startFetchingData() {
        vm.startListing().launchCollect(retry = if (retryCount < 3) ::retryListing else null) { (position, items) ->
            onFetchedItems(position, items)
        }

        retryCount++

        if (adapter.totalSize == 0) {
            vm.fetchAround(0)
        }
    }

    private fun retryListing() {
        vm.stopListing()
        launch { startFetchingData() }
    }
}
