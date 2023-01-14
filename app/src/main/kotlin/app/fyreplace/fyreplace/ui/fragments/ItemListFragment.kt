package app.fyreplace.fyreplace.ui.fragments

import android.content.ComponentCallbacks2
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemListBinding
import app.fyreplace.fyreplace.events.NetworkConnectionWasChangedEvent
import app.fyreplace.fyreplace.events.PositionalEvent
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.ui.adapters.holders.ItemHolder
import app.fyreplace.fyreplace.viewmodels.ItemListViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlin.math.max

abstract class ItemListFragment<Item, Items, VH : ItemHolder> :
    DynamicListFragment<Item>(R.layout.fragment_item_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView get() = if (::bd.isInitialized) bd.root else null
    abstract override val vm: ItemListViewModel<Item, Items>
    protected lateinit var bd: FragmentItemListBinding
    private lateinit var adapter: ItemListAdapter<Item, VH>
    private var retryCount = 0

    abstract fun makeAdapter(): ItemListAdapter<Item, VH>

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

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.resetTo(vm.items)
        bd.recyclerView.adapter = adapter
        bd.swipe.setOnRefreshListener {
            stopListing()
            resetListing()
            startListing()
        }

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
        startListing()
    }

    override fun onStop() {
        stopListing()
        super.onStop()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND || !isVisible) {
            resetListing()
        }
    }

    override fun addItem(event: PositionalEvent<Item>) {
        adapter.add(event.position, event.event.item)
    }

    override fun updateItem(event: PositionalEvent<Item>) {
        adapter.update(event.position, event.event.item)
    }

    override fun removeItem(event: PositionalEvent<Item>) {
        adapter.remove(event.position)

        if (adapter.itemCount < ItemListViewModel.PAGE_SIZE) launch {
            vm.fetchMore()
        }
    }

    override fun onChildViewAttachedToWindow(view: View) {
        val childPosition = bd.recyclerView.getChildAdapterPosition(view)

        if (adapter.itemCount - childPosition < ItemListViewModel.PAGE_SIZE) launch {
            vm.fetchMore()
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit

    protected fun resetListing() {
        adapter.removeAll()
        vm.reset()
    }

    protected fun refreshListing(pauseAction: (() -> Unit)? = null) {
        stopListing()
        pauseAction?.invoke()
        refreshAllEventHandlers()
        resetListing()
        startListing()
    }

    private fun retryListing() {
        stopListing()
        startListing()
    }

    private fun startListing() {
        launch {
            vm.startListing().launchCollect(retry = if (retryCount < 3) ::retryListing else null) {
                bd.swipe.isRefreshing = false
                adapter.addAll(it)
            }

            retryCount++
            val manualCount = max(vm.manuallyAddedCount, 0)

            if (adapter.itemCount - manualCount <= 0) {
                vm.reset()
                vm.fetchMore()
            }
        }
    }

    private fun stopListing() = vm.stopListing()
}
