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
import app.fyreplace.fyreplace.databinding.FragmentItemListBinding
import app.fyreplace.fyreplace.ui.adapters.ItemHolder
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.ItemChangeViewModel
import app.fyreplace.fyreplace.viewmodels.ItemListViewModel

abstract class ItemListFragment<Item : Any, Items : Any, VH : ItemHolder> :
    BaseFragment(R.layout.fragment_item_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView by lazy { if (::bd.isInitialized) bd.root else null }
    protected abstract val icvm: ItemChangeViewModel<Item>
    protected abstract val vm: ItemListViewModel<Item, Items>
    protected abstract val emptyText: String
    protected lateinit var bd: FragmentItemListBinding
    private lateinit var adapter: ItemListAdapter<Item, VH>

    abstract fun makeAdapter(): ItemListAdapter<Item, VH>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        icvm.addedItems.launchCollect { (p, i) -> vm.add(p, i) }
        icvm.updatedItems.launchCollect { (p, i) -> vm.update(p, i) }
        icvm.removedPositions.launchCollect { vm.remove(it) }
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
        bd.emptyText.text = emptyText
        bd.recycler.setHasFixedSize(true)
        bd.recycler.addOnChildAttachStateChangeListener(this)
        bd.recycler.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = makeAdapter()
        adapter.addAll(vm.items)
        bd.recycler.adapter = adapter
        bd.swipe.setOnRefreshListener {
            adapter.removeAll()
            vm.reset()
            launch { vm.fetchMore() }
        }

        icvm.addedItems.launchCollect(viewLifecycleOwner.lifecycleScope) { (p, i) ->
            adapter.add(p, i)
        }
        icvm.updatedItems.launchCollect(viewLifecycleOwner.lifecycleScope) { (p, i) ->
            adapter.update(p, i)
        }
        icvm.removedPositions.launchCollect(viewLifecycleOwner.lifecycleScope) {
            adapter.remove(it)
        }
    }

    override fun onDestroyView() {
        bd.recycler.removeOnChildAttachStateChangeListener(this)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
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

    override fun onStop() {
        super.onStop()
        vm.stopListing()
    }

    override fun onChildViewAttachedToWindow(view: View) {
        val childPosition = bd.recycler.getChildAdapterPosition(view)

        if (adapter.itemCount - childPosition < ItemListViewModel.PAGE_SIZE) launch {
            vm.fetchMore()
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit
}
