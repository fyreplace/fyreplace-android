package app.fyreplace.fyreplace.test.viewmodels

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
        val viewModel = makeViewModel(
            identifierMinLength = minLength,
            identifierMaxLength = maxLength,
            randomCodeMinLength = 8
        )

        for (i in 0..<minLength) {
            viewModel.updateIdentifier("a".repeat(i))
            runCurrent()
            assertFalse(viewModel.canSubmit)
        }

        for (i in minLength..maxLength) {
            viewModel.updateIdentifier("a".repeat(i))
            runCurrent()
            assertTrue(viewModel.canSubmit)
        }

        viewModel.updateIdentifier("a".repeat(maxLength + 1))
        runCurrent()
        assertEquals(maxLength, viewModel.identifier.length)
        assertTrue(viewModel.canSubmit)
    }

    @Test
    fun `Invalid identifier produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        backgroundScope.launch { eventBus.events.collect() }
        viewModel.updateIdentifier(FakeUsersEndpointApi.BAD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertFalse(viewModel.isWaitingForRandomCode)
    }

    @Test
    fun `Valid identifier produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        backgroundScope.launch { eventBus.events.collect() }
        viewModel.updateIdentifier(FakeUsersEndpointApi.GOOD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
        assertTrue(viewModel.isWaitingForRandomCode)
    }

    @Test
    fun `Password identifier produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        backgroundScope.launch { eventBus.events.collect() }
        viewModel.updateIdentifier(FakeTokensEndpointApi.PASSWORD_IDENTIFIER)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertFalse(viewModel.isWaitingForRandomCode)
    }

    @Test
    fun `Random code must have correct length`() = runTest {
        val minLength = 10
        val viewModel = makeViewModel(
            identifierMinLength = 3,
            identifierMaxLength = 50,
            randomCodeMinLength = minLength
        )

        viewModel.updateIdentifier(FakeUsersEndpointApi.GOOD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()

        for (i in 0..<minLength) {
            viewModel.updateRandomCode("a".repeat(i))
            runCurrent()
            assertFalse(viewModel.canSubmit)
        }

        viewModel.updateRandomCode("a".repeat(minLength))
        runCurrent()
        assertTrue(viewModel.canSubmit)
    }

    @Test
    fun `Invalid random code produces failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        backgroundScope.launch { eventBus.events.collect() }
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
        val viewModel = makeViewModel(eventBus)
        backgroundScope.launch { eventBus.events.collect() }
        viewModel.updateIdentifier(FakeUsersEndpointApi.GOOD_USERNAME)
        runCurrent()
        viewModel.submit()
        runCurrent()
        viewModel.updateRandomCode(FakeTokensEndpointApi.GOOD_SECRET)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
    }

    private fun TestScope.makeViewModel(
        eventBus: EventBus = FakeEventBus(),
        identifierMinLength: Int = 3,
        identifierMaxLength: Int = 50,
        randomCodeMinLength: Int = 8
    ) = LoginViewModel(
        state = SavedStateHandle(),
        eventBus = eventBus,
        storeResolver = FakeStoreResolver(),
        apiResolver = FakeApiResolver(),
        resourceResolver = FakeResourceResolver(
            mapOf(
                R.integer.username_min_length to identifierMinLength,
                R.integer.email_max_length to identifierMaxLength,
                R.integer.random_code_min_length to randomCodeMinLength
            )
        ),
        secretsHandler = FakeSecretsHandler()
    ).also { runCurrent() }
}
