package app.fyreplace.fyreplace.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class LoadingViewModel : BaseViewModel() {
    private val mIsLoading = MutableStateFlow(false)
    val isLoading = mIsLoading.asStateFlow()

    protected suspend fun <T> whileLoading(block: suspend () -> T): T {
        try {
            mIsLoading.value = true
            return block()
        } finally {
            mIsLoading.value = false
        }
    }
}
