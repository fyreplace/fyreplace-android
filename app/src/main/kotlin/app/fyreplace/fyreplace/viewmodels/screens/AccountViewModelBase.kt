package app.fyreplace.fyreplace.viewmodels.screens

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.SecretsHandler
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.extensions.update
import app.fyreplace.fyreplace.protos.Account
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AccountViewModelBase(
    protected val state: SavedStateHandle,
    eventBus: EventBus,
    storeResolver: StoreResolver,
    protected val resourceResolver: ResourceResolver,
    private val secretsHandler: SecretsHandler
) : ApiViewModelBase(eventBus, storeResolver) {
    private val mIsLoading = MutableStateFlow(false)

    abstract val isFirstStepValid: Flow<Boolean>

    val isWaitingForRandomCode = storeResolver.accountStore.data
        .map { it.isWaitingForRandomCode }
        .asState(false)
    val isLoading = mIsLoading.asStateFlow()
    val randomCode: StateFlow<String> =
        state.getStateFlow(::randomCode.name, "")
    val canSubmit = isWaitingForRandomCode
        .flatMapLatest { if (it) isRandomCodeValid else isFirstStepValid }
        .combine(isLoading) { canSubmit, isLoading -> canSubmit && !isLoading }
        .distinctUntilChanged()
        .asState(false)

    private val isRandomCodeValid = randomCode
        .map { it.isNotBlank() && it.length >= resourceResolver.getInteger(R.integer.random_code_min_length) }

    fun updateRandomCode(randomCode: String) {
        state[::randomCode.name] = randomCode
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
                mIsLoading.update { true }
                block()
            } finally {
                mIsLoading.update { false }
            }
        }

    protected fun onEmailSent(isRegistering: Boolean) {
        viewModelScope.launch {
            storeResolver.accountStore.update {
                setIsWaitingForRandomCode(true)
                setIsRegistering(isRegistering)
            }

            showEmailSentTip()
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
                        } catch (e: Exception) {
                            eventBus.publish(Event.Snackbar(R.string.account_tip_email_sent_action_failed))
                        }
                    }
                )
            ))
    }
}
