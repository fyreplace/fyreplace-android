package app.fyreplace.fyreplace.test.screens

import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.events.FailureEvent
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
        val eventBus = EventBus()
        val viewModel = MainViewModel(eventBus)

        backgroundScope.launch { viewModel.currentFailure.collect() }

        runCurrent()
        assertNull(viewModel.currentFailure.value)

        eventBus.publish(FailureEvent())
        eventBus.publish(FailureEvent())
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
