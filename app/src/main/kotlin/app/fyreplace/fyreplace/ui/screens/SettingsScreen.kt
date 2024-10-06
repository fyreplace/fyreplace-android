package app.fyreplace.fyreplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.api.data.User
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.fakes.placeholder
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.settings.AvatarPreference
import app.fyreplace.fyreplace.ui.views.settings.Preference
import app.fyreplace.fyreplace.ui.views.settings.Section
import app.fyreplace.fyreplace.viewmodels.screens.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

        Section(stringResource(R.string.settings_header_profile)) {
            AvatarPreference(
                user = currentUser,
                onUpdateAvatar = viewModel::updateAvatar,
                onRemoveAvatar = viewModel::removeAvatar
            )

            Preference(
                title = stringResource(R.string.settings_logout),
                summary = stringResource(R.string.settings_logout_summary),
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                onClick = viewModel::logout
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            viewModel = SettingsViewModel(
                SavedStateHandle().apply {
                    this[SettingsViewModel::currentUser.name] = User.placeholder
                },
                FakeEventBus(),
                FakeStoreResolver(),
                FakeApiResolver()
            )
        )
    }
}
