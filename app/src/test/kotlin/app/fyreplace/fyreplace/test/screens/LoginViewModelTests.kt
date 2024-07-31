package app.fyreplace.fyreplace.test.screens

import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.test.fakes.FakeResourceResolver
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
        val resolver = FakeResourceResolver(
            mapOf(
                R.integer.username_min_length to minLength,
                R.integer.email_max_length to maxLength
            )
        )
        val viewModel = LoginViewModel(resolver)

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
}
