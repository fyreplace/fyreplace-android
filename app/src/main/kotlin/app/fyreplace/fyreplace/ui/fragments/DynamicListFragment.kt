package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import app.fyreplace.fyreplace.viewmodels.EventsViewModel
import app.fyreplace.fyreplace.viewmodels.events.ItemPositionalEvent
import app.fyreplace.fyreplace.viewmodels.events.PositionalEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

abstract class DynamicListFragment<Item>(contentLayoutId: Int) : BaseFragment(contentLayoutId) {
    protected val evm by activityViewModels<EventsViewModel>()
    protected abstract val addedItems: Flow<ItemPositionalEvent<Item>>
    protected abstract val updatedItems: Flow<ItemPositionalEvent<Item>>
    protected abstract val removedItems: Flow<PositionalEvent>
    private val vmEventJobs = mutableListOf<Job>()
    private val viewEventJobs = mutableListOf<Job>()

    abstract fun addItem(position: Int, item: Item, toView: Boolean)

    abstract fun updateItem(position: Int, item: Item, toView: Boolean)

    abstract fun removeItem(position: Int, toView: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshViewModelEventsHandlers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshViewEventsHandlers()
    }

    protected fun refreshEventHandlers() {
        refreshViewModelEventsHandlers()
        refreshViewEventsHandlers()
    }

    private fun refreshViewModelEventsHandlers() {
        vmEventJobs.forEach { it.cancel() }
        vmEventJobs.clear()

        vmEventJobs.add(addedItems.launchCollect {
            addItem(it.position, it.item, false)
        })
        vmEventJobs.add(updatedItems.launchCollect {
            updateItem(it.position, it.item, false)
        })
        vmEventJobs.add(removedItems.launchCollect {
            removeItem(it.position, false)
        })
    }

    private fun refreshViewEventsHandlers() {
        viewEventJobs.forEach { it.cancel() }
        viewEventJobs.clear()

        viewEventJobs.add(addedItems.launchCollect(viewLifecycleOwner.lifecycleScope) {
            addItem(it.position, it.item, true)
        })
        viewEventJobs.add(updatedItems.launchCollect(viewLifecycleOwner.lifecycleScope) {
            updateItem(it.position, it.item, true)
        })
        viewEventJobs.add(removedItems.launchCollect(viewLifecycleOwner.lifecycleScope) {
            removeItem(it.position, true)
        })
    }
}
