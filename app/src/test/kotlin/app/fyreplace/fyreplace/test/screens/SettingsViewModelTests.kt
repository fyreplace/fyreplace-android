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
import app.fyreplace.fyreplace.protos.Secrets
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.viewmodels.screens.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTests : TestsBase() {
    @Test
    fun `ViewModel retrieves current user`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        backgroundScope.launch { viewModel.currentUser.collect() }

        runCurrent()
        assertNotNull(viewModel.currentUser.value)
    }

    @Test
    fun `Updating avatar with a too large image produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        runCurrent()
        backgroundScope.launch { viewModel.currentUser.collect() }

        viewModel.updateAvatar(FakeUsersEndpointApi.LARGE_IMAGE_FILE)
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertEquals("", viewModel.currentUser.value?.avatar)
    }

    @Test
    fun `Updating avatar with an invalid produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        runCurrent()
        backgroundScope.launch { viewModel.currentUser.collect() }

        viewModel.updateAvatar(FakeUsersEndpointApi.NOT_IMAGE_FILE)
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertEquals("", viewModel.currentUser.value?.avatar)
    }

    @Test
    fun `Updating avatar with a valid image produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        runCurrent()
        backgroundScope.launch { viewModel.currentUser.collect() }

        viewModel.updateAvatar(FakeUsersEndpointApi.NORMAL_IMAGE_FILE)
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertEquals(
            FakeUsersEndpointApi.NORMAL_IMAGE_FILE.path,
            viewModel.currentUser.value?.avatar
        )
    }

    @Test
    fun `Removing avatar produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        runCurrent()
        viewModel.updateAvatar(FakeUsersEndpointApi.NORMAL_IMAGE_FILE)
        backgroundScope.launch { viewModel.currentUser.collect() }

        viewModel.removeAvatar()
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertEquals("", viewModel.currentUser.value?.avatar)
    }

    @Test
    fun `Bio must have correct length`() = runTest {
        val maxLength = 30
        val viewModel = makeViewModel(FakeEventBus(), maxLength)
        runCurrent()
        backgroundScope.launch { viewModel.canUpdateBio.collect() }

        viewModel.updatePendingBio("a")
        runCurrent()
        assertTrue(viewModel.canUpdateBio.value)
        viewModel.updatePendingBio("a".repeat(maxLength))
        runCurrent()
        assertTrue(viewModel.canUpdateBio.value)
        viewModel.updatePendingBio("a".repeat(maxLength + 1))
        runCurrent()
        assertFalse(viewModel.canUpdateBio.value)
    }

    @Test
    fun `Bio must be different`() = runTest {
        val viewModel = makeViewModel(FakeEventBus())
        runCurrent()
        backgroundScope.launch { viewModel.canUpdateBio.collect() }

        viewModel.updatePendingBio("Hello")
        runCurrent()
        assertTrue(viewModel.canUpdateBio.value)
        viewModel.updateBio()
        runCurrent()
        assertFalse(viewModel.canUpdateBio.value)
    }

    @Test
    fun `Updating bio produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        runCurrent()
        backgroundScope.launch { viewModel.currentUser.collect() }

        viewModel.updatePendingBio("Hello")
        viewModel.updateBio()
        runCurrent()
        assertEquals(0, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertEquals("Hello", viewModel.currentUser.value?.bio)
    }

    private suspend fun makeViewModel(eventBus: EventBus, bioMaxLength: Int = 100) =
        SettingsViewModel(
            SavedStateHandle(),
            eventBus,
            FakeResourceResolver(mapOf(R.integer.bio_max_length to bioMaxLength)),
            FakeStoreResolver(
                secrets = Secrets.newBuilder()
                    .setToken(FakeSecretsHandler().encrypt(FakeTokensEndpointApi.TOKEN)).build()
            ),
            FakeApiResolver()
        )
}
