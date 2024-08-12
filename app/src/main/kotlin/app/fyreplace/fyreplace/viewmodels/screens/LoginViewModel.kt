package app.fyreplace.fyreplace.viewmodels.screens

import app.fyreplace.api.TokensEndpointApi
import app.fyreplace.api.data.NewTokenCreation
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.Endpoint
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.events.FailureEvent
import app.fyreplace.fyreplace.viewmodels.Failure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class LoginViewModel @Inject constructor(
    eventBus: EventBus,
    private val resourceResolver: ResourceResolver,
    private val tokensEndpoint: Endpoint<TokensEndpointApi>
) : AccountViewModelBase(eventBus) {
    val identifier = MutableStateFlow("")
    val canSubmit = identifier
        .map { it.isNotBlank() && it.length >= resourceResolver.getInteger(R.integer.username_min_length) }
        .combine(isLoading) { canSubmit, isLoading -> canSubmit && !isLoading }
        .asState(false)

    override fun handle(failure: Failure) = when (failure.code) {
        404 -> FailureEvent(R.string.login_error_404_title, R.string.login_error_404_message)
        else -> FailureEvent()
    }

    fun updateIdentifier(value: String) {
        val maxLength = resourceResolver.getInteger(R.integer.email_max_length)
        identifier.value = value.substring(0, min(maxLength, value.length))
    }

    fun sendEmail() = callWhileLoading(tokensEndpoint) {
        createNewToken(NewTokenCreation(identifier.value)).check()
    }
}
