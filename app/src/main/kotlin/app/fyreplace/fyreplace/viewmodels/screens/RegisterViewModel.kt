package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.update
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RegisterViewModel @Inject constructor(
    state: SavedStateHandle,
    eventBus: EventBus,
    resourceResolver: ResourceResolver,
    storeResolver: StoreResolver
) : AccountViewModelBase(state, eventBus, resourceResolver, storeResolver) {
    private val storedUsername = storeResolver.accountStore.data
        .map { it.username }
        .asState("")
    private val storedEmail = storeResolver.accountStore.data
        .map { it.email }
        .asState("")

    private val typedUsername: Flow<String> =
        state.getStateFlow(::typedUsername.name, "")
            .onEach { viewModelScope.launch { storeResolver.accountStore.update { setUsername(it) } } }
    private val typedEmail: Flow<String> =
        state.getStateFlow(::typedEmail.name, "")
            .onEach { viewModelScope.launch { storeResolver.accountStore.update { setEmail(it) } } }

    val username = hasStartedTyping
        .flatMapLatest { if (it) typedUsername else storedUsername }
        .asState("")
    val email = hasStartedTyping
        .flatMapLatest { if (it) typedEmail else storedEmail }
        .asState("")

    override val isFirstStepValid = username
        .combine(email) { username, email ->
            val usernameMinLength = resourceResolver.getInteger(R.integer.username_min_length)
            val emailMinLength = resourceResolver.getInteger(R.integer.email_min_length)
            return@combine (username.isNotBlank()
                    && username.length >= usernameMinLength
                    && email.isNotBlank()
                    && email.length >= emailMinLength
                    && email.contains('@'))
        }
        .distinctUntilChanged()
        .asState(false)

    fun updateUsername(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.username_max_length)
        startTyping()
        state[::typedUsername.name] = value.substring(0, min(maxLength, value.length))
    }

    fun updateEmail(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.email_max_length)
        startTyping()
        state[::typedEmail.name] = value.substring(0, min(maxLength, value.length))
    }
}
