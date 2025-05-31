package app.fyreplace.fyreplace.viewmodels.screens

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.SecretsHandler
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.update
import app.fyreplace.fyreplace.protos.Account
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(SavedStateHandleSaveableApi::class)
abstract class AccountViewModelBase(
    state: SavedStateHandle,
    eventBus: EventBus,
    storeResolver: StoreResolver,
    protected val resourceResolver: ResourceResolver,
    private val secretsHandler: SecretsHandler
) : ApiViewModelBase(eventBus, storeResolver) {
    abstract val isFirstStepValid: Boolean

    val isWaitingForRandomCode by storeResolver.accountStore.data
        .map { it.isWaitingForRandomCode }
        .asState(false)
    var isLoading by state.saveable { mutableStateOf(false) }
        private set
    var randomCode by state.saveable { mutableStateOf("") }
        private set
    val canSubmit
        get() = !isLoading && if (isWaitingForRandomCode) isRandomCodeValid else isFirstStepValid

    private var lastDeepLinkRandomCode by state.saveable { mutableStateOf<String?>(null) }
    private val isRandomCodeValid
        get() = randomCode.isNotBlank()
                && randomCode.length >= resourceResolver.getInteger(R.integer.random_code_min_length)

    fun updateRandomCode(randomCode: String) {
        this.randomCode = randomCode
    }

    fun trySubmitDeepLinkRandomCode(randomCode: String) {
        if (randomCode == lastDeepLinkRandomCode || !isWaitingForRandomCode) {
            return
        }

        lastDeepLinkRandomCode = randomCode

        if (randomCode.isNotEmpty()) {
            updateRandomCode(randomCode)
            submit()
        }
    }

    fun submit() = when {
        isWaitingForRandomCode -> createToken()
        else -> sendEmail()
    }

    fun cancel() {
        viewModelScope.launch {
            storeResolver.accountStore.update {
                setIsWaitingForRandomCode(false)
                setIsRegistering(false)
            }
        }
    }

    protected abstract fun sendEmail()

    protected abstract fun createToken()

    protected fun <T> callWhileLoading(api: suspend () -> T, block: suspend T.() -> Unit) =
        call(api) {
            try {
                isLoading = true
                block()
            } finally {
                isLoading = false
            }
        }

    protected fun onEmailSent(isRegistering: Boolean, showEmailTip: Boolean = true) {
        viewModelScope.launch {
            storeResolver.accountStore.update {
                setIsWaitingForRandomCode(true)
                setIsRegistering(isRegistering)
            }

            if (showEmailTip) {
                showEmailSentTip()
            }
        }
    }

    protected fun onTokenCreated(token: String) {
        viewModelScope.launch {
            storeResolver.secretsStore.update { setToken(secretsHandler.encrypt(token)) }
            storeResolver.accountStore.update(Account.Builder::clear)
            updateRandomCode("")
        }
    }

    private suspend fun showEmailSentTip() {
        eventBus.publish(
            Event.Snackbar(
                message = R.string.account_tip_email_sent,
                action = Event.Snackbar.Action(
                    label = R.string.account_tip_email_sent_action,
                    action = {
                        try {
                            startActivity(
                                Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_APP_EMAIL)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (_: Exception) {
                            eventBus.publish(Event.Snackbar(R.string.account_tip_email_sent_action_failed))
                        }
                    }
                )
            )
        )
    }
}
