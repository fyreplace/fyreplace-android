package app.fyreplace.fyreplace.viewmodels.settings

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.protos.Environment
import app.fyreplace.fyreplace.viewmodels.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnvironmentSelectorViewModel @Inject constructor(
    storeResolver: StoreResolver
) : ViewModelBase() {
    private val connectionStore = storeResolver.connectionStoreIn(viewModelScope)

    val environment = connectionStore.data
        .map { it.environment }
        .asState(Environment.UNRECOGNIZED)

    fun updateEnvironment(environment: Environment) {
        viewModelScope.launch {
            connectionStore.updateData {
                it.toBuilder().setEnvironment(environment).build()
            }
        }
    }
}
