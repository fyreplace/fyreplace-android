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
import app.fyreplace.fyreplace.extensions.update
import app.fyreplace.fyreplace.protos.Secrets
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import io.sentry.protocol.User
import kotlinx.coroutines.flow.distinctUntilChanged
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

        viewModelScope.launch {
            storeResolver.secretsStore.data
                .map(Secrets::getToken)
                .distinctUntilChanged()
                .map(ByteString::isEmpty)
                .map(Boolean::not)
                .collect(::storeCurrentUser)
        }
    }

    fun dismiss(failure: Event.Failure) {
        failures.remove(failure)
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

    private fun storeCurrentUser(hasToken: Boolean) = call(apiResolver::users) {
        storeResolver.currentUserStore.update {
            if (hasToken) {
                val currentUser = getCurrentUser().require()
                id = currentUser?.id.toString()
                Sentry.setUser(User().also {
                    it.id = id
                    it.username = currentUser?.username
                })
            } else {
                clearId()
                Sentry.setUser(null)
            }
        }
    }
}
