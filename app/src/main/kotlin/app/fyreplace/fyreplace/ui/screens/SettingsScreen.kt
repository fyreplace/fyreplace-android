package app.fyreplace.fyreplace.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
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
import app.fyreplace.fyreplace.ui.views.settings.AvatarListItem
import app.fyreplace.fyreplace.ui.views.settings.LinkListItem
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
        val isLoadingAvatar by viewModel.isLoadingAvatar.collectAsStateWithLifecycle()

        Section(stringResource(R.string.settings_profile_header)) {
            AvatarListItem(
                user = currentUser,
                isLoading = isLoadingAvatar,
                onUpdateAvatar = viewModel::updateAvatar,
                onRemoveAvatar = viewModel::removeAvatar
            )


            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_profile_logout)) },
                supportingContent = { Text(stringResource(R.string.settings_profile_logout_summary)) },
                leadingContent = { Icon(Icons.AutoMirrored.Outlined.Logout, null) },
                modifier = Modifier.clickable(onClick = viewModel::logout)
            )
        }

        Section(stringResource(R.string.settings_about_header)) {
            LinkListItem(
                title = stringResource(R.string.settings_about_website),
                uri = stringResource(R.string.info_url_website),
                icon = Icons.Outlined.Info
            )

            LinkListItem(
                title = stringResource(R.string.settings_about_terms_of_service),
                uri = stringResource(R.string.info_url_terms_of_service),
                icon = Icons.Outlined.Shield
            )

            LinkListItem(
                title = stringResource(R.string.settings_about_privacy_policy),
                uri = stringResource(R.string.info_url_privacy_policy),
                icon = Icons.Outlined.Lock
            )

            LinkListItem(
                title = stringResource(R.string.settings_about_source_code),
                uri = stringResource(R.string.info_url_source_code),
                icon = Icons.Outlined.Code
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
