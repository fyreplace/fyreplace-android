package app.fyreplace.fyreplace.viewmodels.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import app.fyreplace.api.data.NewTokenCreation
import app.fyreplace.api.data.TokenCreation
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.SecretsHandler
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.update
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min


@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
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
    var identifier by state.saveable { mutableStateOf("") }
        private set

    override val isFirstStepValid
        get() = identifier.isNotBlank()
                && identifier.length >= resourceResolver.getInteger(R.integer.username_min_length)

    init {
        viewModelScope.launch {
            storeResolver.accountStore.data.collect {
                identifier = it.identifier.orEmpty()
            }
        }
    }

    fun updateIdentifier(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.email_max_length)
        val newValue = value.substring(0, min(maxLength, value.length))
        identifier = newValue
        viewModelScope.launch { storeResolver.accountStore.update { setIdentifier(newValue) } }
    }

    override fun sendEmail() = callWhileLoading(apiResolver::tokens) {
        val input = NewTokenCreation(identifier = identifier)
        createNewToken(input).failWith {
            when (it.code) {
                400 -> Event.Failure(
                    R.string.error_400_title,
                    R.string.error_400_message
                )

                403 -> Event.Failure(
                    R.string.login_error_403_title,
                    R.string.login_error_403_message
                ).also {
                    onEmailSent(isRegistering = false, showEmailTip = false)
                }

                404 -> Event.Failure(
                    R.string.login_error_404_title,
                    R.string.login_error_404_message
                )

                else -> Event.Failure()
            }
        } ?: return@callWhileLoading

        onEmailSent(isRegistering = false)
    }

    override fun createToken() = callWhileLoading(apiResolver::tokens) {
        val input = TokenCreation(identifier = identifier, secret = randomCode)
        val token = createToken(input).failWith {
            when (it.code) {
                400 -> Event.Failure(
                    R.string.account_error_create_token_400_title,
                    R.string.account_error_create_token_400_message
                )

                404 -> Event.Failure(
                    R.string.login_error_404_title,
                    R.string.login_error_404_message
                )

                else -> Event.Failure()
            }
        } ?: return@callWhileLoading

        onTokenCreated(token)
        updateIdentifier("")
    }
}