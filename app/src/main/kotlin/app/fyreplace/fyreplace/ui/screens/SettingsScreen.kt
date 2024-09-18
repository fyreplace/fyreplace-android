package app.fyreplace.fyreplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import app.fyreplace.fyreplace.ui.views.Avatar
import app.fyreplace.fyreplace.viewmodels.screens.SettingsViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = dimensionResource(R.dimen.spacing_medium))
    ) {
        val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

        UserInfo(currentUser)
        Button(onClick = viewModel::logout) {
            Text(stringResource(R.string.settings_logout))
        }
    }
}

@Composable
private fun UserInfo(user: User?) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT) }
    val loading = stringResource(R.string.loading)

    Avatar(user = user, tinted = true, size = 128.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = user?.username ?: loading,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = when (user) {
                null -> loading
                else -> stringResource(
                    R.string.settings_date_joined,
                    dateFormatter.format(user.dateCreated)
                )
            },
            style = MaterialTheme.typography.titleMedium
        )
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
