package app.fyreplace.fyreplace.viewmodels.screens

import androidx.lifecycle.viewModelScope
import app.fyreplace.fyreplace.data.StoreResolver
import app.fyreplace.fyreplace.extensions.update
import app.fyreplace.fyreplace.protos.Connection
import app.fyreplace.fyreplace.protos.Secrets
import app.fyreplace.fyreplace.viewmodels.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val storeResolver: StoreResolver
) : ViewModelBase() {
    fun logout() {
        viewModelScope.launch {
            storeResolver.connectionStore.update(Connection.Builder::clear)
            storeResolver.secretsStore.update(Secrets.Builder::clear)
        }
    }
}
