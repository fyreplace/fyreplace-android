package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.SavedStateHandle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.data.ResourceResolver
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AccountViewModelBase(
    protected val state: SavedStateHandle,
    eventBus: EventBus,
    storeResolver: StoreResolver,
    protected val resourceResolver: ResourceResolver
) : ApiViewModelBase(eventBus, storeResolver) {
    private val mIsLoading = MutableStateFlow(false)

    abstract val isFirstStepValid: Flow<Boolean>

    val isWaitingForRandomCode = storeResolver.accountStore.data
        .map { it.isWaitingForRandomCode }
        .asState(false)
    val isLoading = mIsLoading.asStateFlow()
    val randomCode: StateFlow<String> =
        state.getStateFlow(::randomCode.name, "")
    val canSubmit = isWaitingForRandomCode
        .flatMapLatest { if (it) isRandomCodeValid else isFirstStepValid }
        .combine(isLoading) { canSubmit, isLoading -> canSubmit && !isLoading }
        .distinctUntilChanged()
        .asState(false)

    protected val hasStartedTyping: StateFlow<Boolean> =
        state.getStateFlow(::hasStartedTyping.name, false)
    private val isRandomCodeValid = randomCode
        .map { it.isNotBlank() && it.length >= resourceResolver.getInteger(R.integer.random_code_min_length) }

    fun updateRandomCode(randomCode: String) {
        state[::randomCode.name] = randomCode
    }

    protected fun startTyping() {
        state[::hasStartedTyping.name] = true
    }

    protected fun <T> callWhileLoading(api: suspend () -> T, block: suspend T.() -> Unit) =
        call(api) {
            try {
                mIsLoading.update { true }
                block()
            } finally {
                mIsLoading.update { false }
            }
        }
}
