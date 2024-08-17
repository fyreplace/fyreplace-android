package app.fyreplace.fyreplace.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.viewmodels.screens.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Box {
        Button(
            onClick = viewModel::logout,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(stringResource(R.string.settings_logout))
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            viewModel = SettingsViewModel(FakeStoreResolver())
        )
    }
}
