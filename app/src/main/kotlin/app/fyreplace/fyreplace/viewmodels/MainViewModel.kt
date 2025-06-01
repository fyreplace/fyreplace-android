package app.fyreplace.fyreplace.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import app.fyreplace.api.data.EmailVerification
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    state: SavedStateHandle,
    eventBus: EventBus,
    storeResolver: StoreResolver,
    apiResolver: ApiResolver
) : ApiViewModelBase(eventBus, storeResolver, apiResolver) {
    private val failures = mutableStateListOf<Event.Failure>()

    val events = eventBus.events

    val isAuthenticated by storeResolver.secretsStore.data
        .map { !it.token.isEmpty }
        .asState(true)
    val isWaitingForRandomCode by storeResolver.accountStore.data
        .map { it.isWaitingForRandomCode }
        .asState(false)
    val isRegistering by storeResolver.accountStore.data
        .map { it.isRegistering }
        .asState(false)
    val currentFailure
        get() = failures.firstOrNull()
    var verifiedEmail by state.saveable { mutableStateOf("") }
        private set

    init {
        viewModelScope.launch {
            eventBus.events.filterIsInstance<Event.Failure>().collect(failures::add)
        }

        viewModelScope.launch {
            eventBus.events.filterIsInstance<Event.EmailVerification>().collect {
                verifyEmail(it.email, it.randomCode)
            }
        }

        viewModelScope.launch {
            eventBus.events.filterIsInstance<Event.EmailVerified>().collect {
                verifiedEmail = it.email
            }
        }
    }

    fun dismissError() {
        failures.removeAt(0)
    }

    fun dismissVerifiedEmail() {
        verifiedEmail = ""
    }

    fun verifyEmail(email: String, randomCode: String) = call(apiResolver::emails) {
        verifyEmail(EmailVerification(email = email, code = randomCode)).failWith {
            when (it.code) {
                400 -> Event.Failure(
                    R.string.error_400_title,
                    R.string.error_400_message
                )

                404 -> Event.Failure(
                    R.string.main_error_email_verification_404_title,
                    R.string.main_error_email_verification_404_message
                )

                else -> Event.Failure()
            }
        } ?: return@call

        eventBus.publish(Event.EmailVerified(email = email))
    }
}
