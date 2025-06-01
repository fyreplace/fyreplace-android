package app.fyreplace.fyreplace.viewmodels.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import app.fyreplace.api.data.Email
import app.fyreplace.api.data.EmailCreation
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.ApiResolver
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.Event
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class EmailsViewModel @Inject constructor(
    state: SavedStateHandle,
    eventBus: EventBus,
    storeResolver: StoreResolver,
    apiResolver: ApiResolver,
) : ApiViewModelBase(eventBus, storeResolver, apiResolver) {
    val emails = mutableStateListOf<Email>()
    var newEmail by state.saveable { mutableStateOf("") }
        private set
    var unverifiedEmail by state.saveable { mutableStateOf("") }
        private set
    var randomCode by state.saveable { mutableStateOf("") }
        private set

    init {
        viewModelScope.launch {
            eventBus.events.filterIsInstance<Event.EmailVerified>().collect { event ->
                emails.forEachIndexed { i, email ->
                    if (email.email == event.email) {
                        emails[i] = email.copy(verified = true)
                    }
                }
            }
        }
    }

    init {
        loadEmails()
    }

    private fun loadEmails() = call(apiResolver::emails) {
        emails.clear()
        var page = 0

        do {
            val emailsPage = listEmails(page).require() ?: return@call
            emails.addAll(emailsPage)
            page++
        } while (emailsPage.isNotEmpty())
    }

    fun updateNewEmail(email: String) {
        newEmail = email
    }

    fun updateUnverifiedEmail(email: String) {
        unverifiedEmail = email
    }

    fun updateRandomCode(code: String) {
        randomCode = code
    }

    fun addNewEmail() = call(apiResolver::emails) {
        val email = createEmail(EmailCreation(email = newEmail)).failWith {
            when (it.code) {
                400 -> Event.Failure(
                    R.string.emails_error_email_400_title,
                    R.string.emails_error_email_400_message
                )

                409 -> Event.Failure(
                    R.string.emails_error_email_409_title,
                    R.string.emails_error_email_409_message
                )

                else -> Event.Failure()
            }
        } ?: return@call

        emails.add(email)
        updateNewEmail("")
    }

    fun verifyEmail() {
        viewModelScope.launch {
            eventBus.publish(
                Event.EmailVerification(
                    email = unverifiedEmail,
                    randomCode = randomCode
                )
            )
            randomCode = ""
        }
    }
}
