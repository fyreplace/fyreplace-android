package app.fyreplace.fyreplace.legacy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemRandomAccessListBinding
import app.fyreplace.fyreplace.legacy.events.PositionalEvent
import app.fyreplace.fyreplace.legacy.ui.adapters.ItemRandomAccessListAdapter
import app.fyreplace.fyreplace.legacy.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.legacy.viewmodels.ItemRandomAccessListViewModel
import kotlinx.coroutines.FlowPreview

abstract class ItemRandomAccessListFragment<Item, Items : Any, VH : ItemHolder> :
    DynamicListFragment<Item>(R.layout.fragment_item_random_access_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView get() = if (::bd.isInitialized) bd.root else null
    override val em by lazy { vm.em }
    override lateinit var bd: FragmentItemRandomAccessListBinding
    abstract override val vm: ItemRandomAccessListViewModel<Item, Items>
    override val recyclerView get() = bd.recyclerView
    protected lateinit var adapter: ItemRandomAccessListAdapter<Item, VH>

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

        with(recyclerView) {
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
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        recyclerView.removeOnChildAttachStateChangeListener(this)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        startListing()
    }

    override fun onStop() {
        super.onStop()
        stopListing()
    }

    override fun addItem(event: PositionalEvent<Item>) =
        adapter.insert(event.event.item)

    override fun updateItem(event: PositionalEvent<Item>) =
        adapter.update(event.position, event.event.item)

    override fun removeItem(event: PositionalEvent<Item>) = Unit

    override fun onChildViewAttachedToWindow(view: View) {
        val itemPosition = recyclerView.getChildAdapterPosition(view) - 1

        if (vm.items[itemPosition] == null) launch {
            vm.fetchAround(itemPosition)
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit

    override fun startListing() {
        launch { startFetchingData() }
    }

    override fun stopListing() = vm.stopListing()

    open fun onFetchedItems(position: Int, items: List<Item>) {
        adapter.update(position, items)

        if (adapter.totalSize == 0) {
            adapter.setTotalSize(vm.totalSize)
        }
    }

    open suspend fun startFetchingData() {
        vm.startListing()
            .launchCollect(retry = if (retryCount < 3) ::retryListing else null) { (position, items) ->
                retryCount = 0
                onFetchedItems(position, items)
            }

        if (adapter.totalSize == 0) {
            vm.fetchAround(0)
        }
    }
}
