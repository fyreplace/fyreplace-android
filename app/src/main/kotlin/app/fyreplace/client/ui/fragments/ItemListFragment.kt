package app.fyreplace.client.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.fyreplace.client.R
import app.fyreplace.client.databinding.FragmentItemListBinding
import app.fyreplace.client.ui.adapters.ItemListAdapter
import app.fyreplace.client.viewmodels.ItemListViewModel
import java.io.Serializable

abstract class ItemListFragment<Item : Any, Items : Any> :
    BaseFragment(R.layout.fragment_item_list),
    RecyclerView.OnChildAttachStateChangeListener {
    protected abstract val vm: ItemListViewModel<Item, Items>
    protected abstract val emptyText: String
    protected lateinit var adapter: ItemListAdapter<Item, ItemListAdapter.Holder>
    private lateinit var bd: FragmentItemListBinding

    abstract fun makeAdapter(context: Context): ItemListAdapter<Item, ItemListAdapter.Holder>

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
        val color = ResourcesCompat.getColor(resources, R.color.primary, context?.theme)
        bd.swipe.setColorSchemeColors(color)
        bd.recycler.addOnChildAttachStateChangeListener(this)
        bd.recycler.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = makeAdapter(view.context)
        bd.recycler.adapter = adapter
        bd.swipe.setOnRefreshListener {
            adapter.clear()
            bd.emptyText.isVisible = true
            vm.reset()
            launch { vm.fetchMore() }
        }

        launch {
            vm.startListing().launchCollect {
                bd.emptyText.isVisible = false
                bd.swipe.isRefreshing = false
                adapter.add(it)
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

        if (adapter.itemCount - childPosition < ItemListViewModel.pageSize) {
            launch { vm.fetchMore() }
        }
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit

    inner class ItemDeletionNotifier(private val position: Int) : DeletionNotifier {
        override fun onDelete() = adapter.remove(position)
    }
}

interface DeletionNotifier : Serializable {
    fun onDelete()
}
