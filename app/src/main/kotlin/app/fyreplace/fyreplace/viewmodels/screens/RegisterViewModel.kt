package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.fyreplace.api.data.TokenCreation
import app.fyreplace.api.data.UserCreation
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.SecretsHandler
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.update
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class RegisterViewModel @Inject constructor(
    state: SavedStateHandle,
    eventBus: EventBus,
    resourceResolver: ResourceResolver,
    storeResolver: StoreResolver,
    secretsHandler: SecretsHandler,
    private val apiResolver: ApiResolver
) : AccountViewModelBase(state, eventBus, storeResolver, resourceResolver, secretsHandler) {
    val username: StateFlow<String> =
        state.getStateFlow(::username.name, "")
    val email: StateFlow<String> =
        state.getStateFlow(::email.name, "")

    init {
        viewModelScope.launch {
            val account = storeResolver.accountStore.data.first()
            updateUsername(account.username)
            updateEmail(account.email)
        }
    }

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
        val newValue = value.substring(0, min(maxLength, value.length))
        state[::username.name] = newValue
        viewModelScope.launch { storeResolver.accountStore.update { setUsername(newValue) } }
    }

    fun updateEmail(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.email_max_length)
        val newValue = value.substring(0, min(maxLength, value.length))
        state[::email.name] = newValue
        viewModelScope.launch { storeResolver.accountStore.update { setEmail(newValue) } }
    }

    override fun sendEmail() = callWhileLoading(apiResolver::users) {
        val input = UserCreation(email = email.value, username = username.value)
        createUser(input).failWith {
            when (it.code) {
                400 -> when (it.violationReport?.violations?.firstOrNull()?.field) {
                    "createUser.input.username" -> Event.Failure(
                        R.string.register_error_create_user_400_username_title,
                        R.string.register_error_create_user_400_username_message
                    )

                    "createUser.input.email" -> Event.Failure(
                        R.string.register_error_create_user_400_email_title,
                        R.string.register_error_create_user_400_email_message
                    )

                    else -> Event.Failure(
                        R.string.error_400_title,
                        R.string.error_400_message
                    )
                }

                403 -> Event.Failure(
                    R.string.register_error_create_user_403_title,
                    R.string.register_error_create_user_403_message
                )

                409 -> when (it.explainedFailure?.reason) {
                    "username_taken" -> Event.Failure(
                        R.string.register_error_create_user_409_username_title,
                        R.string.register_error_create_user_409_username_message
                    )

                    "email_taken" -> Event.Failure(
                        R.string.register_error_create_user_409_email_title,
                        R.string.register_error_create_user_409_email_message
                    )

                    else -> Event.Failure()
                }

                else -> Event.Failure()
            }
        } ?: return@callWhileLoading

        onEmailSent(isRegistering = true)
    }

    override fun createToken() = callWhileLoading(apiResolver::tokens) {
        val input = TokenCreation(identifier = email.value, secret = randomCode.value)
        val token = createToken(input).failWith {
            when (it.code) {
                400 -> Event.Failure(
                    R.string.account_error_create_token_400_title,
                    R.string.account_error_create_token_400_message
                )

                404 -> Event.Failure(
                    R.string.register_error_create_token_404_title,
                    R.string.register_error_create_token_404_message
                )

                else -> Event.Failure()
            }
        } ?: return@callWhileLoading

        onTokenCreated(token)
        updateUsername("")
        updateEmail("")
    }
}
