package app.fyreplace.fyreplace.viewmodels.screens

import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import app.fyreplace.fyreplace.viewmodels.Failure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class RegisterViewModel @Inject constructor(
    eventBus: EventBus,
    private val resourceResolver: ResourceResolver
) : ApiViewModelBase(eventBus) {
    val username = MutableStateFlow("")
    val email = MutableStateFlow("")
    val canSubmit = username
        .combine(email) { username, email ->
            val usernameMinLength = resourceResolver.getInteger(R.integer.username_min_length)
            val emailMinLength = resourceResolver.getInteger(R.integer.email_min_length)
            return@combine (username.isNotBlank()
                    && username.length >= usernameMinLength
                    && email.isNotBlank()
                    && email.length >= emailMinLength
                    && email.contains('@'))
        }
        .asState(false)

    override fun handle(failure: Failure) = null

    fun updateUsername(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.username_max_length)
        username.value = value.substring(0, min(maxLength, value.length))
    }

    fun updateEmail(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.email_max_length)
        email.value = value.substring(0, min(maxLength, value.length))
    }
}
