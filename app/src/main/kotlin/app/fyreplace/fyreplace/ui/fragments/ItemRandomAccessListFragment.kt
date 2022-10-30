package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemRandomAccessListBinding
import app.fyreplace.fyreplace.events.PositionalEvent
import app.fyreplace.fyreplace.ui.adapters.ItemRandomAccessListAdapter
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.viewmodels.ItemRandomAccessListViewModel

abstract class ItemRandomAccessListFragment<Item, Items, VH : ItemHolder> :
    ScrollingListFragment<Item>(R.layout.fragment_item_random_access_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView get() = if (::bd.isInitialized) bd.root else null
    abstract override val vm: ItemRandomAccessListViewModel<Item, Items>
    protected lateinit var bd: FragmentItemRandomAccessListBinding
    protected lateinit var adapter: ItemRandomAccessListAdapter<Item, VH>

    abstract fun makeAdapter(): ItemRandomAccessListAdapter<Item, VH>

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
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = makeAdapter()
        adapter.resetTo(vm.items)
        bd.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        bd.recyclerView.removeOnChildAttachStateChangeListener(this)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        launch {
            vm.startListing().launchCollect { (index, items) -> onFetchedItems(index, items) }

            if (adapter.totalSize == 0) {
                vm.fetchAround(0)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        vm.stopListing()
    }

    override fun addItem(position: Int, event: PositionalEvent<Item>) =
        adapter.insert(event.event.item)

    override fun updateItem(position: Int, event: PositionalEvent<Item>) =
        adapter.update(position, event.event.item)

    override fun removeItem(position: Int, event: PositionalEvent<Item>) = Unit

    override fun onChildViewAttachedToWindow(view: View) {
        val itemPosition = bd.recyclerView.getChildAdapterPosition(view) - 1

        if (vm.items[itemPosition] == null) launch {
            vm.fetchAround(itemPosition)
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit

    open fun onFetchedItems(index: Int, items: List<Item>) {
        adapter.update(index, items)

        if (adapter.totalSize == 0) {
            adapter.setTotalSize(vm.totalSize)
        }
    }
}
