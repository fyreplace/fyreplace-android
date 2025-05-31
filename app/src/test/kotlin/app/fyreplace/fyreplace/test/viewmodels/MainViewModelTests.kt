package app.fyreplace.fyreplace.test.viewmodels

import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.HotEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.viewmodels.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTests : TestsBase() {
    @Test
    fun `Failures can be dismissed`() = runTest {
        val eventBus = HotEventBus()
        val viewModel = MainViewModel(
            eventBus = eventBus,
            storeResolver = FakeStoreResolver()
        )
        backgroundScope.launch { viewModel.events.collect() }
        runCurrent()
        assertNull(viewModel.currentFailure)
        eventBus.publish(Event.Failure())
        eventBus.publish(Event.Failure())
        runCurrent()
        assertNotNull(viewModel.currentFailure)
        viewModel.dismissError()
        runCurrent()
        assertNotNull(viewModel.currentFailure)
        viewModel.dismissError()
        runCurrent()
        assertNull(viewModel.currentFailure)
    }
}
