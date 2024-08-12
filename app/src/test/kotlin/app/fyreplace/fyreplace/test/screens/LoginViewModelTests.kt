package app.fyreplace.fyreplace.test.screens

import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.events.FailureEvent
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.test.fakes.EndpointFake
import app.fyreplace.fyreplace.test.fakes.ResourceResolverFake
import app.fyreplace.fyreplace.test.fakes.TokensEndpointApiFake
import app.fyreplace.fyreplace.viewmodels.screens.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTests : TestsBase() {
    @Test
    fun `Identifier must have correct length`() = runTest {
        val minLength = 5
        val maxLength = 100
        val eventBus = EventBus()
        val viewModel = makeViewModel(eventBus, minLength, maxLength)

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
        val eventBus = EventBus()
        val viewModel = makeViewModel(eventBus, 3, 50)

        backgroundScope.launch {
            val event = eventBus.events.firstOrNull()
            assertNotNull(event)
            assertEquals(FailureEvent::class, event!!::class)
        }

        viewModel.updateIdentifier(TokensEndpointApiFake.BAD_IDENTIFIER)
        viewModel.sendEmail()
        runCurrent()
    }

    @Test
    fun `Valid identifier produces no failures`() = runTest {
        val eventBus = EventBus()
        val viewModel = makeViewModel(eventBus, 3, 50)

        backgroundScope.launch {
            val event = eventBus.events.firstOrNull()
            assertNull(event)
        }

        viewModel.updateIdentifier(TokensEndpointApiFake.GOOD_IDENTIFIER)
        viewModel.sendEmail()
        runCurrent()
    }

    private fun makeViewModel(
        eventBus: EventBus,
        identifierMinLength: Int,
        identifierMaxLength: Int
    ): LoginViewModel {
        val resolver = ResourceResolverFake(
            mapOf(
                R.integer.username_min_length to identifierMinLength,
                R.integer.email_max_length to identifierMaxLength
            )
        )
        return LoginViewModel(eventBus, resolver, EndpointFake(::TokensEndpointApiFake))
    }
}
