package app.fyreplace.fyreplace.events

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import app.fyreplace.fyreplace.R

interface Event

@Immutable
data class FailureEvent(
    @StringRes val title: Int = R.string.main_error_unknown_title,
    @StringRes val message: Int = R.string.main_error_unknown_message
) : Event
