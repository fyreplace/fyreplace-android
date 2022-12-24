package app.fyreplace.fyreplace.ui.fragments

import android.content.ComponentCallbacks2
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemListBinding
import app.fyreplace.fyreplace.events.PositionalEvent
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.viewmodels.ItemListViewModel
import kotlin.math.max

abstract class ItemListFragment<Item, Items, VH : ItemHolder> :
    DynamicListFragment<Item>(R.layout.fragment_item_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView get() = if (::bd.isInitialized) bd.root else null
    abstract override val vm: ItemListViewModel<Item, Items>
    protected lateinit var bd: FragmentItemListBinding
    private lateinit var adapter: ItemListAdapter<Item, VH>

    abstract fun makeAdapter(): ItemListAdapter<Item, VH>

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
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = makeAdapter()
        adapter.addAll(vm.items)
        bd.recyclerView.adapter = adapter
        bd.swipe.setOnRefreshListener {
            stopListing()
            reset()
            startListing()
        }
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

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN || !isVisible) {
            reset()
        }
    }

    override fun addItem(position: Int, event: PositionalEvent<Item>) {
        adapter.add(position, event.event.item)
    }

    override fun updateItem(position: Int, event: PositionalEvent<Item>) {
        adapter.update(position, event.event.item)
    }

    override fun removeItem(position: Int, event: PositionalEvent<Item>) {
        adapter.remove(position)
    }

    override fun onChildViewAttachedToWindow(view: View) {
        val childPosition = bd.recyclerView.getChildAdapterPosition(view)

        if (adapter.itemCount - childPosition < ItemListViewModel.PAGE_SIZE) launch {
            vm.fetchMore()
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit

    protected fun refreshListing(pauseAction: (() -> Unit)? = null) {
        stopListing()
        pauseAction?.invoke()
        refreshAllEventHandlers()
        reset()
        startListing()
    }

    private fun startListing() {
        launch {
            vm.startListing().launchCollect {
                bd.swipe.isRefreshing = false
                adapter.addAll(it)
            }

            val manualCount = max(vm.manuallyAddedCount, 0)

            if (adapter.itemCount - manualCount <= 0) {
                vm.reset()
                vm.fetchMore()
            }
        }
    }

    private fun stopListing() = vm.stopListing()

    private fun reset() {
        adapter.removeAll()
        vm.reset()
    }
}
