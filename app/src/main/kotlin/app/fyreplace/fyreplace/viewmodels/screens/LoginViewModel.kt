package app.fyreplace.fyreplace.viewmodels.screens

import android.content.Context
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.viewmodels.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModelBase() {
    val identifier = MutableStateFlow("")
    val canSubmit = identifier
        .map { it.isNotBlank() && it.length >= context.resources.getInteger(R.integer.username_min_length) }
        .asState(false)

    fun updateIdentifier(value: String) {
        val maxLength = context.resources.getInteger(R.integer.username_max_length)
        identifier.value = value.substring(0, min(maxLength, value.length))
    }
}
