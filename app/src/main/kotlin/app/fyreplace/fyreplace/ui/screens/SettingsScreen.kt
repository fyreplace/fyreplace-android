package app.fyreplace.fyreplace.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.codePointCount
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeResourceResolver
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.settings.AvatarListItem
import app.fyreplace.fyreplace.ui.views.settings.LinkListItem
import app.fyreplace.fyreplace.ui.views.settings.Section
import app.fyreplace.fyreplace.viewmodels.screens.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), navigateToEmails: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Section(stringResource(R.string.settings_profile_header)) {
            AvatarListItem(
                user = viewModel.currentUser,
                isLoading = viewModel.isLoadingAvatar,
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

        Section(stringResource(R.string.settings_bio_header)) {
            TextField(
                value = viewModel.bio,
                maxLines = 10,
                placeholder = { Text(stringResource(R.string.settings_bio_placeholder)) },
                supportingText = {
                    Text(
                        stringResource(
                            R.string.settings_bio_length,
                            viewModel.bio.codePointCount,
                            integerResource(R.integer.bio_max_length)
                        )
                    )
                },
                onValueChange = viewModel::updatePendingBio,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.spacing_medium))
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.spacing_small))
            ) {
                Button(
                    enabled = viewModel.canUpdateBio,
                    onClick = viewModel::updateBio
                ) {
                    Text(stringResource(R.string.settings_bio_save))
                }
            }
        }

        Section(stringResource(R.string.settings_connection_header)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_connection_emails)) },
                supportingContent = { Text(stringResource(R.string.settings_connection_emails_summary)) },
                leadingContent = { Icon(Icons.Outlined.AlternateEmail, null) },
                modifier = Modifier.clickable(onClick = navigateToEmails)
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
fun SettingsScreenPreview(
    @PreviewParameter(SettingsPreviewProvider::class)
    viewModel: SettingsViewModel
) {
    AppTheme {
        SettingsScreen(
            viewModel = viewModel,
            navigateToEmails = {}
        )
    }
}

private class SettingsPreviewProvider : PreviewParameterProvider<SettingsViewModel> {
    override val values = sequenceOf(
        SettingsViewModel(
            SavedStateHandle(),
            FakeEventBus(),
            FakeResourceResolver(mapOf(R.integer.bio_max_length to 100)),
            FakeStoreResolver(),
            FakeApiResolver()
        )
    )
}
