package app.fyreplace.client.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentItemListBinding
import app.fyreplace.client.ui.adapters.ItemListAdapter
import app.fyreplace.client.viewmodels.ItemListViewModel
import kotlinx.coroutines.flow.map

abstract class ItemListFragment<Item : Any, Items : Any> :
    BaseFragment(R.layout.fragment_item_list) {
    protected abstract val vm: ItemListViewModel<Item, Items>
    protected abstract val emptyText: String
    private lateinit var bd: FragmentItemListBinding

    abstract fun makeAdapter(context: Context): ItemListAdapter<Item, ItemListAdapter.Holder>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.also {
        bd = FragmentItemListBinding.bind(it)
        bd.lifecycleOwner = viewLifecycleOwner
        bd.emptyText.text = emptyText
        val color = ResourcesCompat.getColor(resources, R.color.primary, context?.theme)
        bd.swipe.setColorSchemeColors(color)
        bd.recycler.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = makeAdapter(view.context)
        bd.recycler.adapter = adapter

        adapter.loadStateFlow.map {
            adapter.itemCount == 0 &&
                    it.append is LoadState.NotLoading &&
                    it.append.endOfPaginationReached
        }.launchCollect { bd.emptyText.isVisible = it }

        vm.items.launchCollect {
            bd.swipe.isRefreshing = false
            adapter.submitData(it)
        }

        bd.swipe.setOnRefreshListener(adapter::refresh)
    }
}
