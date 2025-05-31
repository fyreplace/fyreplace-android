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
import app.fyreplace.fyreplace.protos.Secrets
import app.fyreplace.fyreplace.test.TestsBase
import app.fyreplace.fyreplace.viewmodels.screens.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
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
    fun `Loading current user produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
        assertNotNull(viewModel.currentUser)
    }

    @Test
    fun `Updating avatar with a too large image produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        viewModel.updateAvatar(FakeUsersEndpointApi.LARGE_IMAGE_FILE)
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertEquals("", viewModel.currentUser?.avatar)
    }

    @Test
    fun `Updating avatar with an invalid produces a failure`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        viewModel.updateAvatar(FakeUsersEndpointApi.NOT_IMAGE_FILE)
        runCurrent()
        assertEquals(1, eventBus.storedEvents.filterIsInstance<Event.Failure>().count())
        assertEquals("", viewModel.currentUser?.avatar)
    }

    @Test
    fun `Updating avatar with a valid image produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        viewModel.updateAvatar(FakeUsersEndpointApi.NORMAL_IMAGE_FILE)
        runCurrent()
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
        assertEquals(
            FakeUsersEndpointApi.NORMAL_IMAGE_FILE.path,
            viewModel.currentUser?.avatar
        )
    }

    @Test
    fun `Removing avatar produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        viewModel.updateAvatar(FakeUsersEndpointApi.NORMAL_IMAGE_FILE)
        runCurrent()
        viewModel.removeAvatar()
        runCurrent()
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
        assertEquals("", viewModel.currentUser?.avatar)
    }

    @Test
    fun `Bio must have correct length`() = runTest {
        val maxLength = 30
        val viewModel = makeViewModel(bioMaxLength = maxLength)
        viewModel.updatePendingBio("a")
        runCurrent()
        assertTrue(viewModel.canUpdateBio)
        viewModel.updatePendingBio("a".repeat(maxLength))
        runCurrent()
        assertTrue(viewModel.canUpdateBio)
        viewModel.updatePendingBio("a".repeat(maxLength + 1))
        runCurrent()
        assertFalse(viewModel.canUpdateBio)
    }

    @Test
    fun `Bio must be different`() = runTest {
        val viewModel = makeViewModel()
        viewModel.updatePendingBio("Hello")
        runCurrent()
        assertTrue(viewModel.canUpdateBio)
        viewModel.updateBio()
        runCurrent()
        assertFalse(viewModel.canUpdateBio)
    }

    @Test
    fun `Updating bio produces no failures`() = runTest {
        val eventBus = FakeEventBus()
        val viewModel = makeViewModel(eventBus)
        viewModel.updatePendingBio("Hello")
        viewModel.updateBio()
        runCurrent()
        assertTrue(eventBus.storedEvents.filterIsInstance<Event.Failure>().isEmpty())
        assertEquals("Hello", viewModel.currentUser?.bio)
    }

    private suspend fun TestScope.makeViewModel(
        eventBus: EventBus = FakeEventBus(),
        bioMaxLength: Int = 100
    ) = SettingsViewModel(
        state = SavedStateHandle(),
        eventBus = eventBus,
        resourceResolver = FakeResourceResolver(mapOf(R.integer.bio_max_length to bioMaxLength)),
        storeResolver = FakeStoreResolver(
            secrets = Secrets.newBuilder()
                .setToken(FakeSecretsHandler().encrypt(FakeTokensEndpointApi.TOKEN)).build()
        ),
        apiResolver = FakeApiResolver()
    ).also { runCurrent() }
}
