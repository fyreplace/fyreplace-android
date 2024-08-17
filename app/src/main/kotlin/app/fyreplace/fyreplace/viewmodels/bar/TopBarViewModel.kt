package app.fyreplace.fyreplace.viewmodels.bar

import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.viewmodels.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class TopBarViewModel @Inject constructor(
    storeResolver: StoreResolver
) : ViewModelBase() {
    val isWaitingForRandomCode = storeResolver.accountStore.data
        .map { it.isWaitingForRandomCode }
        .asState(false)
}
