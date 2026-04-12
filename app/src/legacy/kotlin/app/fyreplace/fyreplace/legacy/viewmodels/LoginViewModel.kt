package app.fyreplace.fyreplace.legacy.viewmodels

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.IntegerRes
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.legacy.extensions.dedupe
import app.fyreplace.fyreplace.legacy.extensions.storeAuthToken
import app.fyreplace.fyreplace.legacy.grpc.defaultClient
import app.fyreplace.protos.AccountServiceClient
import app.fyreplace.protos.ConnectionCredentials
import app.fyreplace.protos.Email
import app.fyreplace.protos.UserCreation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    override val preferences: SharedPreferences,
    private val resources: Resources,
    private val accountService: AccountServiceClient
) : LoadingViewModel() {
    private val mIsRegistering = MutableStateFlow(false)
    val isRegistering = mIsRegistering.asStateFlow()
    val email = MutableStateFlow("")
    val username = MutableStateFlow("")
    val conditionsAccepted = MutableStateFlow(false)
    val canProceed = isRegistering
        .combine(conditionsAccepted) { registering, accepted -> registering to accepted }
        .combine(username) { (registering, accepted), username ->
            !registering || (accepted && username.between(
                R.integer.username_min_size,
                R.integer.username_max_size
            ))
        }
        .combine(email) { canProceed, email ->
            canProceed && email.between(
                R.integer.email_min_size,
                R.integer.email_max_size
            )
        }
        .asState(false)

    fun setIsRegistering(registering: Boolean) {
        mIsRegistering.value = registering
    }

    suspend fun register() = whileLoading {
        accountService.Create()
            .dedupe()
            .execute(UserCreation(email = email.value, username = username.value))
    }

    suspend fun login() = whileLoading {
        accountService.SendConnectionEmail()
            .dedupe()
            .execute(Email(email = email.value))
    }

    suspend fun login(password: String) = whileLoading {
        val token = accountService.Connect()
            .dedupe()
            .execute(
                ConnectionCredentials(
                    email = email.value,
                    password = password,
                    client = defaultClient
                )
            )
        preferences.storeAuthToken(token)
    }

    private fun String.between(@IntegerRes a: Int, @IntegerRes b: Int) =
        isNotBlank() && length in resources.getInteger(a)..resources.getInteger(b)
}
