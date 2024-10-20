package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.extensions.orDefault
import app.fyreplace.fyreplace.extensions.update
import app.fyreplace.fyreplace.protos.Environment
import app.fyreplace.fyreplace.viewmodels.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnvironmentViewModel @Inject constructor(
    private val storeResolver: StoreResolver
) : ViewModelBase() {
    val environment = storeResolver.connectionStore.data
        .map { it.environment.orDefault }
        .asState(Environment.UNSPECIFIED)

    fun updateEnvironment(environment: Environment) {
        viewModelScope.launch {
            storeResolver.connectionStore.update { setEnvironment(environment) }
        }
    }
}
