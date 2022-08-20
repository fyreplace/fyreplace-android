package app.fyreplace.fyreplace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import app.fyreplace.fyreplace.viewmodels.DynamicListViewModel
import kotlinx.coroutines.Job

abstract class DynamicListFragment<Item>(contentLayoutId: Int) : BaseFragment(contentLayoutId) {
    protected abstract val vm: DynamicListViewModel<Item>
    private val eventJobs = mutableListOf<Job>()

    abstract fun addItem(position: Int, item: Item)

    abstract fun updateItem(position: Int, item: Item)

    abstract fun removeItem(position: Int)

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

        eventJobs.add(vm.addedItems.launchCollect(viewLifecycleOwner.lifecycleScope) {
            addItem(it.position, it.item)
        })
        eventJobs.add(vm.updatedItems.launchCollect(viewLifecycleOwner.lifecycleScope) {
            updateItem(it.position, it.item)
        })
        eventJobs.add(vm.removedItems.launchCollect(viewLifecycleOwner.lifecycleScope) {
            removeItem(it.position)
        })
    }
}
