package app.fyreplace.fyreplace.viewmodels.screens

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import app.fyreplace.fyreplace.protos.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
    state: SavedStateHandle,
    eventBus: EventBus,
    resourceResolver: ResourceResolver,
    storeResolver: StoreResolver,
    private val secretsHandler: SecretsHandler,
    private val apiResolver: ApiResolver
) : AccountViewModelBase(state, eventBus, storeResolver, resourceResolver) {
    private val storedIdentifier = storeResolver.accountStore.data
        .map { it.identifier }
        .asState("")
    private val typedIdentifier: Flow<String> =
        state.getStateFlow(::typedIdentifier.name, "")
            .onEach { viewModelScope.launch { storeResolver.accountStore.update { setIdentifier(it) } } }

    val identifier = hasStartedTyping
        .flatMapLatest { if (it) typedIdentifier else storedIdentifier }
        .asState("")

    override val isFirstStepValid = identifier
        .map { it.isNotBlank() && it.length >= resourceResolver.getInteger(R.integer.username_min_length) }

    fun updateIdentifier(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.email_max_length)
        startTyping()
        state[::typedIdentifier.name] = value.substring(0, min(maxLength, value.length))
    }

    fun submit() {
        if (isWaitingForRandomCode.value) {
            createToken()
        } else {
            sendEmail()
        }
    }

    fun cancel() {
        viewModelScope.launch {
            storeResolver.accountStore.update { setIsWaitingForRandomCode(false) }
        }
    }

    private fun sendEmail() = callWhileLoading(apiResolver::tokens) {
        createNewToken(NewTokenCreation(identifier.value)).failWith {
            when (it.code) {
                400 -> Event.Failure(
                    R.string.error_400_title,
                    R.string.error_400_message
                )

                404 -> Event.Failure(
                    R.string.login_error_404_title,
                    R.string.login_error_404_message
                )

                else -> Event.Failure()
            }
        } ?: return@callWhileLoading

        viewModelScope.launch {
            storeResolver.accountStore.update { setIsWaitingForRandomCode(true) }
            eventBus.publish(Event.Snackbar(
                message = R.string.login_tip_email_sent,
                action = Event.Snackbar.Action(
                    label = R.string.login_tip_email_sent_action,
                    action = {
                        try {
                            startActivity(
                                Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_APP_EMAIL)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (e: Exception) {
                            eventBus.publish(Event.Snackbar(R.string.login_tip_email_sent_action_failed))
                        }
                    }
                )
            ))
        }
    }

    private fun createToken() = callWhileLoading(apiResolver::tokens) {
        val token = createToken(TokenCreation(identifier.value, randomCode.value)).failWith {
            when (it.code) {
                400 -> Event.Failure(
                    R.string.login_error_create_token_400_title,
                    R.string.login_error_create_token_400_message
                )

                404 -> Event.Failure(
                    R.string.login_error_404_title,
                    R.string.login_error_404_message
                )

                else -> Event.Failure()
            }
        } ?: return@callWhileLoading

        viewModelScope.launch {
            storeResolver.secretsStore.update { setToken(secretsHandler.encrypt(token)) }
            storeResolver.accountStore.update(Account.Builder::clear)
        }
    }
}
