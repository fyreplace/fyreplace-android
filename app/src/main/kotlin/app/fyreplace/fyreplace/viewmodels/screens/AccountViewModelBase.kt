package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.SavedStateHandle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.api.Endpoint
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.math.min

abstract class AccountViewModelBase(
    protected val state: SavedStateHandle,
    eventBus: EventBus,
    protected val resourceResolver: ResourceResolver,
    protected val storeResolver: StoreResolver
) : ApiViewModelBase(eventBus) {
    private val mIsLoading = MutableStateFlow(false)

    val isWaitingForRandomCode = storeResolver.accountStore.data
        .map { it.isWaitingForRandomCode }
        .asState(false)
    val isLoading = mIsLoading.asStateFlow()
    val randomCode: StateFlow<String> =
        state.getStateFlow(::randomCode.name, "")

    protected val hasStartedTyping: StateFlow<Boolean> =
        state.getStateFlow(::hasStartedTyping.name, false)

    fun updateRandomCode(randomCode: String) {
        val maxLength = resourceResolver.getInteger(R.integer.random_code_length)
        state[::randomCode.name] = randomCode.substring(0, min(maxLength, randomCode.length))
    }

    protected fun startTyping() {
        state[::hasStartedTyping.name] = true
    }

    protected fun <T> callWhileLoading(api: Endpoint<T>, block: suspend T.() -> Unit) = call(api) {
        try {
            mIsLoading.update { true }
            block()
        } finally {
            mIsLoading.update { false }
        }
    }
}
