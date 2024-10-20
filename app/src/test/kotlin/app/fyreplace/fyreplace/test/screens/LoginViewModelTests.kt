package app.fyreplace.fyreplace.test.screens

import androidx.lifecycle.SavedStateHandle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeResourceResolver
import app.fyreplace.fyreplace.fakes.FakeSecretsHandler
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.fakes.api.FakeTokensEndpointApi
import app.fyreplace.fyreplace.fakes.api.FakeUsersEndpointApi
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.viewmodels.screens.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTests : TestsBase() {
    @Test
    fun `Identifier must have correct length`() = runTest {
        val minLength = 5
        val maxLength = 100
        val viewModel = makeViewModel(FakeEventBus(), minLength, maxLength, 8)
        backgroundScope.launch { viewModel.canSubmit.collect() }

        for (i in 0..<minLength) {
            viewModel.updateIdentifier("a".repeat(i))
            runCurrent()
            assertFalse(viewModel.canSubmit.value)
        }

        for (i in minLength..maxLength) {
            viewModel.updateIdentifier("a".repeat(i))
            runCurrent()
            assertTrue(viewModel.canSubmit.value)
        }

        viewModel.updateIdentifier("a".repeat(maxLength + 1))
        runCurrent()
        assertEquals(maxLength, viewModel.identifier.value.length)
        assertTrue(viewModel.canSubmit.value)
    }

    @Test
    fun `Invalid identifier produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus, 3, 50, 8)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }
        backgroundScope.launch { viewModel.isWaitingForRandomCode.collect() }

        viewModel.updateIdentifier(FakeUsersEndpointApi.BAD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertFalse(viewModel.isWaitingForRandomCode.value)
    }

    @Test
    fun `Valid identifier produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus, 3, 50, 8)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }
        backgroundScope.launch { viewModel.isWaitingForRandomCode.collect() }

        viewModel.updateIdentifier(FakeUsersEndpointApi.GOOD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertTrue(viewModel.isWaitingForRandomCode.value)
    }

    @Test
    fun `Password identifier produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus, 3, 50, 8)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }
        backgroundScope.launch { viewModel.isWaitingForRandomCode.collect() }

        viewModel.updateIdentifier(FakeTokensEndpointApi.PASSWORD_IDENTIFIER)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertTrue(viewModel.isWaitingForRandomCode.value)
    }

    @Test
    fun `Random code must have correct length`() = runTest {
        val minLength = 10
        val viewModel = makeViewModel(FakeEventBus(), 3, 50, minLength)
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateIdentifier(FakeUsersEndpointApi.GOOD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()

        for (i in 0..<minLength) {
            viewModel.updateRandomCode("a".repeat(i))
            runCurrent()
            assertFalse(viewModel.canSubmit.value)
        }

        viewModel.updateRandomCode("a".repeat(minLength))
        runCurrent()
        assertTrue(viewModel.canSubmit.value)
    }

    @Test
    fun `Invalid random code produces failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus, 3, 50, 8)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateIdentifier(FakeUsersEndpointApi.GOOD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()
        viewModel.updateRandomCode(FakeTokensEndpointApi.BAD_SECRET)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
    }

    @Test
    fun `Valid random code produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus, 3, 50, 8)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateIdentifier(FakeUsersEndpointApi.GOOD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()
        viewModel.updateRandomCode(FakeTokensEndpointApi.GOOD_SECRET)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
    }

    private fun TestScope.makeViewModel(
        eventBus: EventBus,
        identifierMinLength: Int,
        identifierMaxLength: Int,
        randomCodeMinLength: Int
    ) = LoginViewModel(
        state = SavedStateHandle(),
        eventBus = eventBus,
        resourceResolver = FakeResourceResolver(
            mapOf(
                R.integer.username_min_length to identifierMinLength,
                R.integer.email_max_length to identifierMaxLength,
                R.integer.random_code_min_length to randomCodeMinLength
            )
        ),
        storeResolver = FakeStoreResolver(),
        secretsHandler = FakeSecretsHandler(),
        apiResolver = FakeApiResolver()
    ).also { runCurrent() }
}
