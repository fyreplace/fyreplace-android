package app.fyreplace.fyreplace.viewmodels.settings

import android.content.Context
import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.data.connectionStore
import app.fyreplace.fyreplace.protos.Environment
import app.fyreplace.fyreplace.viewmodels.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnvironmentSelectorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModelBase() {
    val environment = context.connectionStore.data
        .map { it.environment }
        .asState(Environment.UNRECOGNIZED)

    fun updateEnvironment(environment: Environment) {
        viewModelScope.launch {
            context.connectionStore.updateData {
                it.toBuilder().setEnvironment(environment).build()
            }
        }
    }
}
