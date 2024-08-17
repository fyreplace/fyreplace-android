package app.fyreplace.fyreplace.test.screens

import androidx.lifecycle.SavedStateHandle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeResourceResolver
import app.fyreplace.fyreplace.fakes.FakeSecretsHandler
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.fakes.FakeTokensEndpointApi
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.viewmodels.screens.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
        val viewModel = makeViewModel(FakeEventBus(), minLength, maxLength)
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
        val viewModel = makeViewModel(eventBus, 3, 50)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateIdentifier(FakeTokensEndpointApi.BAD_IDENTIFIER)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
    }

    @Test
    fun `Valid identifier produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus, 3, 50)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateIdentifier(FakeTokensEndpointApi.GOOD_IDENTIFIER)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
    }

    @Test
    fun `Random code must have correct length`() = runTest {
        val viewModel = makeViewModel(FakeEventBus(), 3, 50)
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateIdentifier(FakeTokensEndpointApi.GOOD_IDENTIFIER)
        runCurrent()
        viewModel.submit()
        runCurrent()
        viewModel.updateRandomCode("12345")
        runCurrent()
        assertFalse(viewModel.canSubmit.value)
        viewModel.updateRandomCode("123456")
        runCurrent()
        assertTrue(viewModel.canSubmit.value)
        viewModel.updateRandomCode("1234567")
        runCurrent()
        assertEquals(6, viewModel.randomCode.value.length)
    }

    @Test
    fun `Invalid random code produces failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus, 3, 50)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateIdentifier(FakeTokensEndpointApi.GOOD_IDENTIFIER)
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
        val viewModel = makeViewModel(eventBus, 3, 50)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateIdentifier(FakeTokensEndpointApi.GOOD_IDENTIFIER)
        runCurrent()
        viewModel.submit()
        runCurrent()
        viewModel.updateRandomCode(FakeTokensEndpointApi.GOOD_SECRET)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
    }

    private fun makeViewModel(
        eventBus: EventBus,
        identifierMinLength: Int,
        identifierMaxLength: Int
    ) = LoginViewModel(
        state = SavedStateHandle(),
        eventBus = eventBus,
        resourceResolver = FakeResourceResolver(
            mapOf(
                R.integer.username_min_length to identifierMinLength,
                R.integer.email_max_length to identifierMaxLength,
                R.integer.random_code_length to 6
            )
        ),
        storeResolver = FakeStoreResolver(),
        secretsHandler = FakeSecretsHandler(),
        tokensEndpoint = FakeTokensEndpointApi()
    )
}
