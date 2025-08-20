package app.fyreplace.fyreplace.viewmodels.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import app.fyreplace.api.data.TokenCreation
import app.fyreplace.api.data.UserCreation
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.SecretsHandler
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class RegisterViewModel @Inject constructor(
    state: SavedStateHandle,
    eventBus: EventBus,
    resourceResolver: ResourceResolver,
    storeResolver: StoreResolver,
    secretsHandler: SecretsHandler,
    apiResolver: ApiResolver
) : AccountViewModelBase(
    state,
    eventBus,
    storeResolver,
    apiResolver,
    resourceResolver,
    secretsHandler
) {
    var username by state.saveable { mutableStateOf("") }
        private set
    var email by state.saveable { mutableStateOf("") }
        private set
    var hasAcceptedTerms by state.saveable { mutableStateOf(false) }
        private set

    override val isFirstStepValid
        get() = hasAcceptedTerms
                && username.isNotBlank()
                && username.length >= resourceResolver.getInteger(R.integer.username_min_length)
                && email.contains('@')
                && email.length >= resourceResolver.getInteger(R.integer.email_min_length)

    init {
        viewModelScope.launch {
            storeResolver.accountStore.data.collect {
                username = it.username
                email = it.email
            }
        }
    }

    fun updateUsername(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.username_max_length)
        val newValue = value.substring(0, min(maxLength, value.length))
        username = newValue
        viewModelScope.launch {
            storeResolver.accountStore.updateData {
                it.toBuilder().setUsername(newValue).build()
            }
        }
    }

    fun updateEmail(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.email_max_length)
        val newValue = value.substring(0, min(maxLength, value.length))
        email = newValue
        viewModelScope.launch {
            storeResolver.accountStore.updateData {
                it.toBuilder().setEmail(newValue).build()
            }
        }
    }

    fun updateHasAcceptedTerms(value: Boolean) {
        hasAcceptedTerms = value
    }

    override fun sendEmail() = callWhileLoading(apiResolver::users) {
        val input = UserCreation(email = email, username = username)
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
        val input = TokenCreation(identifier = email, secret = randomCode)
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
