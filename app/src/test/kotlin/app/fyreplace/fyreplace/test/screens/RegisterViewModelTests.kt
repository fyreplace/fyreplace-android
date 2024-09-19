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
import app.fyreplace.fyreplace.viewmodels.screens.RegisterViewModel
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
class RegisterViewModelTests : TestsBase() {
    @Test
    fun `Username must have correct length`() = runTest {
        val (minLength, maxLength, viewModel) = makeViewModel(FakeEventBus())
        viewModel.updateEmail("email@example")
        backgroundScope.launch { viewModel.canSubmit.collect() }

        for (i in 0..<minLength) {
            viewModel.updateUsername("a".repeat(i))
            runCurrent()
            assertFalse(viewModel.canSubmit.value)
        }

        for (i in minLength..maxLength) {
            viewModel.updateUsername("a".repeat(i))
            runCurrent()
            assertTrue(viewModel.canSubmit.value)
        }

        viewModel.updateUsername("a".repeat(maxLength + 1))
        runCurrent()
        assertEquals(maxLength, viewModel.username.value.length)
        assertTrue(viewModel.canSubmit.value)
    }

    @Test
    fun `Email must have correct length`() = runTest {
        val (minLength, maxLength, viewModel) = makeViewModel(FakeEventBus())
        viewModel.updateUsername("Example")
        backgroundScope.launch { viewModel.canSubmit.collect() }

        for (i in 0..<minLength) {
            viewModel.updateEmail("@".repeat(i))
            runCurrent()
            assertFalse(viewModel.canSubmit.value)
        }

        for (i in minLength..maxLength) {
            viewModel.updateEmail("@".repeat(i))
            runCurrent()
            assertTrue(viewModel.canSubmit.value)
        }

        viewModel.updateEmail("@".repeat(maxLength + 1))
        runCurrent()
        assertEquals(maxLength, viewModel.email.value.length)
        assertTrue(viewModel.canSubmit.value)
    }

    @Test
    fun `Email must have @`() = runTest {
        val (_, _, viewModel) = makeViewModel(FakeEventBus())
        viewModel.updateUsername("Example")
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateEmail("email")
        runCurrent()
        assertFalse(viewModel.canSubmit.value)
        viewModel.updateEmail("email@example")
        runCurrent()
        assertTrue(viewModel.canSubmit.value)
    }

    @Test
    fun `Invalid username produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val (_, _, viewModel) = makeViewModel(eventBus)
        val invalidValues = listOf(
            FakeUsersEndpointApi.BAD_USERNAME,
            FakeUsersEndpointApi.RESERVED_USERNAME,
            FakeUsersEndpointApi.USED_USERNAME
        )
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }
        backgroundScope.launch { viewModel.isWaitingForRandomCode.collect() }

        viewModel.updateEmail(FakeUsersEndpointApi.GOOD_EMAIL)

        for (i in invalidValues.indices) {
            viewModel.updateUsername(invalidValues[i])
            runCurrent()
            viewModel.submit()
            runCurrent()
            assertEquals(i + 1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
            assertFalse(viewModel.isWaitingForRandomCode.value)
        }
    }

    @Test
    fun `Invalid email produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val (_, _, viewModel) = makeViewModel(eventBus)
        val invalidValues = listOf(
            FakeUsersEndpointApi.BAD_EMAIL,
            FakeUsersEndpointApi.USED_EMAIL
        )
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }
        backgroundScope.launch { viewModel.isWaitingForRandomCode.collect() }

        viewModel.updateUsername(FakeUsersEndpointApi.GOOD_USERNAME)

        for (i in invalidValues.indices) {
            viewModel.updateEmail(invalidValues[i])
            runCurrent()
            viewModel.submit()
            runCurrent()
            assertEquals(i + 1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
            assertFalse(viewModel.isWaitingForRandomCode.value)
        }
    }

    @Test
    fun `Valid username and email produce no failures`() = runTest {
        val eventBus = FakeEventBus()
        val (_, _, viewModel) = makeViewModel(eventBus)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }
        backgroundScope.launch { viewModel.isWaitingForRandomCode.collect() }

        viewModel.updateUsername(FakeUsersEndpointApi.GOOD_USERNAME)
        viewModel.updateEmail(FakeUsersEndpointApi.GOOD_EMAIL)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertTrue(viewModel.isWaitingForRandomCode.value)
    }

    @Test
    fun `Random code must have correct length`() = runTest {
        val (minLength, _, viewModel) = makeViewModel(FakeEventBus())
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateUsername(FakeUsersEndpointApi.GOOD_USERNAME)
        viewModel.updateEmail(FakeUsersEndpointApi.GOOD_EMAIL)
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
        val (_, _, viewModel) = makeViewModel(eventBus)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateUsername(FakeUsersEndpointApi.GOOD_USERNAME)
        viewModel.updateEmail(FakeUsersEndpointApi.GOOD_EMAIL)
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
        val (_, _, viewModel) = makeViewModel(eventBus)
        backgroundScope.launch { eventBus.events.collect() }
        backgroundScope.launch { viewModel.canSubmit.collect() }

        viewModel.updateUsername(FakeUsersEndpointApi.GOOD_USERNAME)
        viewModel.updateEmail(FakeUsersEndpointApi.GOOD_EMAIL)
        runCurrent()
        viewModel.submit()
        runCurrent()
        viewModel.updateRandomCode(FakeTokensEndpointApi.GOOD_SECRET)
        runCurrent()
        viewModel.submit()
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
    }

    private fun TestScope.makeViewModel(eventBus: EventBus): Triple<Int, Int, RegisterViewModel> {
        val minLength = 5
        val maxLength = 100
        val resources = FakeResourceResolver(
            mapOf(
                R.integer.username_min_length to minLength,
                R.integer.username_max_length to maxLength,
                R.integer.email_min_length to minLength,
                R.integer.email_max_length to maxLength,
                R.integer.random_code_min_length to minLength
            )
        )
        val viewModel = RegisterViewModel(
            state = SavedStateHandle(),
            eventBus = eventBus,
            resourceResolver = resources,
            storeResolver = FakeStoreResolver(),
            secretsHandler = FakeSecretsHandler(),
            apiResolver = FakeApiResolver()
        )
        runCurrent()
        return Triple(minLength, maxLength, viewModel)
    }
}
