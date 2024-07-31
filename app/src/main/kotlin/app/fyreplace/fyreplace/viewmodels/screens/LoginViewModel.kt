package app.fyreplace.fyreplace.viewmodels.screens

import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.viewmodels.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val resourceResolver: ResourceResolver
) : ViewModelBase() {
    val identifier = MutableStateFlow("")
    val canSubmit = identifier
        .map { it.isNotBlank() && it.length >= resourceResolver.getInteger(R.integer.username_min_length) }
        .asState(false)

    fun updateIdentifier(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.email_max_length)
        identifier.value = value.substring(0, min(maxLength, value.length))
    }
}
