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
import app.fyreplace.fyreplace.ui.adapters.ItemHolder
import app.fyreplace.fyreplace.ui.adapters.ItemRandomAccessListAdapter
import app.fyreplace.fyreplace.viewmodels.ItemRandomAccessListViewModel

abstract class ItemRandomAccessListFragment<Item : Any, Items : Any, VH : ItemHolder> :
    ScrollingFragment(R.layout.fragment_item_random_access_list),
    RecyclerView.OnChildAttachStateChangeListener {
    override val rootView by lazy { if (::bd.isInitialized) bd.root else null }
    protected abstract val vm: ItemRandomAccessListViewModel<Item, Items>
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
        adapter.resetTo(vm.items)
        bd.recycler.adapter = adapter
        vm.totalSize.launchCollect(viewLifecycleOwner.lifecycleScope, adapter::setTotalSize)
    }

    override fun onDestroyView() {
        bd.recycler.removeOnChildAttachStateChangeListener(this)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        launch {
            vm.startListing().launchCollect { (index, items) -> adapter.update(index, items) }

            if (adapter.itemCount <= 1) {
                vm.fetchAround(0)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        vm.stopListing()
    }

    override fun onChildViewAttachedToWindow(view: View) {
        val itemPosition = bd.recycler.getChildAdapterPosition(view) - 1

        if (vm.items[itemPosition] == null) launch {
            vm.fetchAround(itemPosition)
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit
}
