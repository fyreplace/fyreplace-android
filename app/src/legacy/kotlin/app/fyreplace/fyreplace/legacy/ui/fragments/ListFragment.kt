package app.fyreplace.fyreplace.legacy.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import app.fyreplace.fyreplace.legacy.events.EventsManager
import app.fyreplace.fyreplace.legacy.events.NetworkConnectionWasChangedEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlin.time.Duration.Companion.seconds

abstract class ListFragment(contentLayoutId: Int) : ScrollingListFragment(contentLayoutId) {
    abstract val em: EventsManager

    protected var retryCount = 0

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        em.events.filterIsInstance<NetworkConnectionWasChangedEvent>()
            .drop(1)
            .debounce(1.seconds)
            .launchCollect(viewLifecycleOwner.lifecycleScope) { refreshListing() }
    }

    protected abstract fun startListing()

    protected abstract fun stopListing()

    protected open fun refreshListing() {
        stopListing()
        startListing()
    }

    protected fun retryListing() {
        stopListing()
        retryCount++
        startListing()
    }
}
