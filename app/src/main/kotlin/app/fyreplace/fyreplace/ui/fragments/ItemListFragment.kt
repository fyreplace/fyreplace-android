package app.fyreplace.fyreplace.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.databinding.FragmentItemListBinding
import app.fyreplace.fyreplace.ui.adapters.ItemListAdapter
import app.fyreplace.fyreplace.viewmodels.ItemChangeViewModel
import app.fyreplace.fyreplace.viewmodels.ItemListViewModel

abstract class ItemListFragment<Item : Any, Items : Any, VH : ItemListAdapter.Holder> :
    BaseFragment(R.layout.fragment_item_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView by lazy { bd.root }
    protected abstract val icvm: ItemChangeViewModel<Item>
    protected abstract val vm: ItemListViewModel<Item, Items>
    protected abstract val emptyText: String
    private lateinit var bd: FragmentItemListBinding
    private lateinit var adapter: ItemListAdapter<Item, VH>

    abstract fun makeAdapter(context: Context): ItemListAdapter<Item, VH>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        icvm.addedItems.launchCollect(fragmentLifecycleScope) { (p, i) ->
            vm.add(p, i)
            adapter.add(p, i)
        }
        icvm.updatedItems.launchCollect(fragmentLifecycleScope) { (p, i) ->
            vm.update(p, i)
            adapter.update(p, i)
        }
        icvm.removedPositions.launchCollect(fragmentLifecycleScope) {
            vm.remove(it)
            adapter.remove(it)
        }
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
        adapter = makeAdapter(view.context)
        adapter.addAll(vm.items)
        bd.emptyText.isVisible = vm.items.isEmpty()
        bd.recycler.adapter = adapter
        bd.swipe.setOnRefreshListener {
            adapter.removeAll()
            bd.emptyText.isVisible = true
            vm.reset()
            launch { vm.fetchMore() }
        }

        launch {
            vm.startListing().launchCollect {
                bd.emptyText.isVisible = it.isEmpty()
                bd.swipe.isRefreshing = false
                adapter.addAll(it)
            }

            if (adapter.itemCount == 0) {
                vm.fetchMore()
            }
        }
    }

    override fun onDestroyView() {
        bd.recycler.removeOnChildAttachStateChangeListener(this)
        vm.stopListing()
        super.onDestroyView()
    }

    override fun onChildViewAttachedToWindow(view: View) {
        val childPosition = bd.recycler.getChildAdapterPosition(view)

        if (adapter.itemCount - childPosition < ItemListViewModel.PAGE_SIZE) {
            launch { vm.fetchMore() }
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit
}
