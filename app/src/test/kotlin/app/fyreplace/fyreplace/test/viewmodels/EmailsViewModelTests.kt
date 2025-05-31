package app.fyreplace.fyreplace.test.viewmodels

import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.viewmodels.screens.EmailsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    private fun TestScope.makeViewModel(eventBus: EventBus = FakeEventBus()) = EmailsViewModel(
        eventBus = eventBus,
        storeResolver = FakeStoreResolver(),
        apiResolver = FakeApiResolver()
    ).also { runCurrent() }
}
