package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import app.fyreplace.fyreplace.events.ItemEvent
import app.fyreplace.fyreplace.viewmodels.DynamicListViewModel
import kotlinx.coroutines.Job

abstract class DynamicListFragment<Item>(contentLayoutId: Int) : BaseFragment(contentLayoutId) {
    abstract override val vm: DynamicListViewModel<Item>
    private val eventJobs = mutableListOf<Job>()

    abstract fun addItem(position: Int, event: ItemEvent<Item>)

    abstract fun updateItem(position: Int, event: ItemEvent<Item>)

    abstract fun removeItem(position: Int, event: ItemEvent<Item>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.refreshEventHandlers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshEventHandlers()
    }

    protected fun refreshAllEventHandlers() {
        vm.refreshEventHandlers()
        refreshEventHandlers()
    }

    private fun refreshEventHandlers() {
        eventJobs.forEach { it.cancel() }
        eventJobs.clear()

        eventJobs.add(vm.addedPositions.launchCollect(viewLifecycleOwner.lifecycleScope) {
            addItem(it.position, it)
        })
        eventJobs.add(vm.updatedPositions.launchCollect(viewLifecycleOwner.lifecycleScope) {
            updateItem(it.position, it)
        })
        eventJobs.add(vm.removedPositions.launchCollect(viewLifecycleOwner.lifecycleScope) {
            removeItem(it.position, it)
        })
    }
}
