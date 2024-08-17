package app.fyreplace.fyreplace.test.screens

import androidx.lifecycle.SavedStateHandle
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
            state = SavedStateHandle(),
            eventBus = eventBus,
            storeResolver = FakeStoreResolver()
        )
        backgroundScope.launch { viewModel.currentFailure.collect() }

        runCurrent()
        assertNull(viewModel.currentFailure.value)

        eventBus.publish(Event.Failure())
        eventBus.publish(Event.Failure())
        runCurrent()
        assertNotNull(viewModel.currentFailure.value)

        viewModel.dismissError()
        runCurrent()
        assertNotNull(viewModel.currentFailure.value)

        viewModel.dismissError()
        runCurrent()
        assertNull(viewModel.currentFailure.value)
    }
}
