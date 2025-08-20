package app.fyreplace.fyreplace.events

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import app.fyreplace.fyreplace.R
import kotlinx.parcelize.Parcelize

sealed interface Event {
    @Immutable
    @Parcelize
    data class Failure(
        @StringRes val title: Int = R.string.error_unknown_title,
        @StringRes val message: Int = R.string.error_unknown_message
    ) : Event, Parcelable

    @Immutable
    data class Snackbar(
        @StringRes val message: Int,
        val action: Action? = null
    ) : Event {
        data class Action(
            @StringRes val label: Int,
            val action: suspend Context.() -> Unit
        )
    }

    @Immutable
    data class Connection(val randomCode: String, val isRegistering: Boolean) : Event

    @Immutable
    data class EmailVerification(val email: String, val randomCode: String) : Event

    @Immutable
    data class EmailVerified(val email: String) : Event
}
