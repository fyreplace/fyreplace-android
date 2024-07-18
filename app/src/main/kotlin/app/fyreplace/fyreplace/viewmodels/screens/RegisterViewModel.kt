package app.fyreplace.fyreplace.viewmodels.screens

import android.content.Context
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.viewmodels.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModelBase() {
    val username = MutableStateFlow("")
    val email = MutableStateFlow("")
    val canSubmit = username
        .combine(email) { username, email ->
            val usernameMaxLength = context.resources.getInteger(R.integer.username_min_length)
            val emailMaxLength = context.resources.getInteger(R.integer.email_min_length)
            return@combine (username.isNotBlank()
                    && username.length >= usernameMaxLength
                    && email.isNotBlank()
                    && email.length >= emailMaxLength
                    && email.contains('@'))
        }
        .asState(false)

    fun updateUsername(value: String) {
        val maxLength = context.resources.getInteger(R.integer.username_max_length)
        username.value = value.substring(0, min(maxLength, value.length))
    }

    fun updateEmail(value: String) {
        val maxLength = context.resources.getInteger(R.integer.email_max_length)
        email.value = value.substring(0, min(maxLength, value.length))
    }
}
