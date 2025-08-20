package app.fyreplace.fyreplace.test.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.fyreplace.api.data.Email
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.HotEventBus
import app.fyreplace.fyreplace.extensions.make
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.fakes.api.FakeTokensEndpointApi
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.viewmodels.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTests : TestsBase() {
    @Test
    fun `Failures can be dismissed`() = runTest {
        val eventBus = HotEventBus()
        val viewModel = MainViewModel(
            state = SavedStateHandle(),
            eventBus = eventBus,
            storeResolver = FakeStoreResolver(),
            apiResolver = FakeApiResolver()
        )
        backgroundScope.launch { viewModel.events.collect() }
        runCurrent()
        assertNull(viewModel.currentFailure)
        eventBus.publish(Event.Failure())
        eventBus.publish(Event.Failure())
        runCurrent()
        assertNotNull(viewModel.currentFailure)
        viewModel.dismiss(viewModel.currentFailure!!)
        runCurrent()
        assertNotNull(viewModel.currentFailure)
        viewModel.dismiss(viewModel.currentFailure!!)
        runCurrent()
        assertNull(viewModel.currentFailure)
    }

    @Test
    fun `Invalid random code produces failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = MainViewModel(
            state = SavedStateHandle(),
            eventBus = eventBus,
            storeResolver = FakeStoreResolver(),
            apiResolver = FakeApiResolver()
        )
        val email = Email.make(verified = false)
        viewModel.verifyEmail(email.email, FakeTokensEndpointApi.BAD_SECRET)
        runCurrent()
        assertFalse(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
    }

    @Test
    fun `Valid random code produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = MainViewModel(
            state = SavedStateHandle(),
            eventBus = eventBus,
            storeResolver = FakeStoreResolver(),
            apiResolver = FakeApiResolver()
        )
        val email = Email.make(verified = false)
        viewModel.verifyEmail(email.email, FakeTokensEndpointApi.GOOD_SECRET)
        runCurrent()
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
    }
}
