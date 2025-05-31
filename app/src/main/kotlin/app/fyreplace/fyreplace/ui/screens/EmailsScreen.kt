package app.fyreplace.fyreplace.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.viewmodels.screens.EmailsViewModel

@Composable
fun EmailsScreen(viewModel: EmailsViewModel = hiltViewModel()) {
    LazyColumn {
        items(viewModel.emails) { email ->
            ListItem(
                headlineContent = { Text(email.email) },
                supportingContent = {
                    if (email.main) {
                        Text(stringResource(R.string.emails_main))
                    }
                }
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun EmailsScreenPreview(
    @PreviewParameter(EmailsPreviewProvider::class)
    viewModel: EmailsViewModel
) {
    EmailsScreen(viewModel = viewModel)
}

private class EmailsPreviewProvider : PreviewParameterProvider<EmailsViewModel> {
    override val values = sequenceOf(
        EmailsViewModel(
            eventBus = FakeEventBus(),
            storeResolver = FakeStoreResolver(),
            apiResolver = FakeApiResolver()
        )
    )
}
