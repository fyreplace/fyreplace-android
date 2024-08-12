package app.fyreplace.fyreplace.viewmodels.screens

import app.fyreplace.fyreplace.api.Endpoint
import app.fyreplace.fyreplace.events.EventBus
import app.fyreplace.fyreplace.viewmodels.ApiViewModelBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class AccountViewModelBase(eventBus: EventBus) : ApiViewModelBase(eventBus) {
    private val mIsLoading = MutableStateFlow(false)

    val isLoading = mIsLoading.asStateFlow()

    protected fun <T> callWhileLoading(api: Endpoint<T>, block: suspend T.() -> Unit) =
        call(api) {
            try {
                mIsLoading.value = true
                block()
            } finally {
                mIsLoading.value = false
            }
        }
}
