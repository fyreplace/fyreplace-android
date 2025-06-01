package app.fyreplace.fyreplace.test.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.fakes.api.FakeUsersEndpointApi
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.viewmodels.screens.EmailsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailsViewModelTests : TestsBase() {
    @Test
    fun `Loading emails produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
        assertEquals(3, viewModel.emails.size)
    }

    @Test
    fun `Invalid email produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        runCurrent()
        val emailCount = viewModel.emails.size
        viewModel.updateNewEmail(FakeUsersEndpointApi.BAD_EMAIL)
        viewModel.addNewEmail()
        runCurrent()
        assertFalse(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
        assertEquals(emailCount, viewModel.emails.size)
    }

    @Test
    fun `Valid email produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        runCurrent()
        val emailCount = viewModel.emails.size
        viewModel.updateNewEmail(FakeUsersEndpointApi.GOOD_EMAIL)
        viewModel.addNewEmail()
        runCurrent()
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
        assertEquals(emailCount + 1, viewModel.emails.size)
    }

    private fun TestScope.makeViewModel(eventBus: EventBus = FakeEventBus()) = EmailsViewModel(
        state = SavedStateHandle(),
        eventBus = eventBus,
        storeResolver = FakeStoreResolver(),
        apiResolver = FakeApiResolver()
    ).also { runCurrent() }
}
