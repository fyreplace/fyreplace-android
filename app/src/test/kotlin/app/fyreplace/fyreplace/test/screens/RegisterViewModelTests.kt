package app.fyreplace.fyreplace.test.screens

import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.test.fakes.ResourceResolverFake
import app.fyreplace.fyreplace.viewmodels.screens.RegisterViewModel
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
class RegisterViewModelTests : TestsBase() {
    @Test
    fun `Username must have correct length`() = runTest {
        val minLength = 5
        val maxLength = 100
        val resolver = ResourceResolverFake(
            mapOf(
                R.integer.username_min_length to minLength,
                R.integer.username_max_length to maxLength,
                R.integer.email_min_length to minLength,
                R.integer.email_max_length to maxLength
            )
        )
        val viewModel = RegisterViewModel(EventBus(), resolver)
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
        val minLength = 5
        val maxLength = 100
        val resolver = ResourceResolverFake(
            mapOf(
                R.integer.username_min_length to minLength,
                R.integer.username_max_length to maxLength,
                R.integer.email_min_length to minLength,
                R.integer.email_max_length to maxLength
            )
        )
        val viewModel = RegisterViewModel(EventBus(), resolver)
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
        val minLength = 5
        val maxLength = 100
        val resolver = ResourceResolverFake(
            mapOf(
                R.integer.username_min_length to minLength,
                R.integer.username_max_length to maxLength,
                R.integer.email_min_length to minLength,
                R.integer.email_max_length to maxLength
            )
        )
        val viewModel = RegisterViewModel(EventBus(), resolver)
        viewModel.updateUsername("Example")

        backgroundScope.launch { viewModel.canSubmit.collect() }


        viewModel.updateEmail("email")
        runCurrent()
        assertFalse(viewModel.canSubmit.value)
        viewModel.updateEmail("email@example")
        runCurrent()
        assertTrue(viewModel.canSubmit.value)
    }
}
