package app.fyreplace.fyreplace.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AlternateEmail
import androidx.compose.material.icons.twotone.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.fakes.FakeApiResolver
import app.fyreplace.fyreplace.fakes.FakeEventBus
import app.fyreplace.fyreplace.fakes.FakeStoreResolver
import app.fyreplace.fyreplace.ui.Clicker
import app.fyreplace.fyreplace.viewmodels.screens.EmailsViewModel

@Composable
fun EmailsScreen(
    mainActionClicker: Clicker,
    viewModel: EmailsViewModel = hiltViewModel()
) {
    var showAddEmail by remember { mutableStateOf(false) }
    var showVerifyEmail by remember { mutableStateOf(false) }
    val unverifiedCount by remember { derivedStateOf { viewModel.emails.count { !it.verified } } }

    LazyColumn {
        items(viewModel.emails) { email ->
            ListItem(
                headlineContent = { Text(email.email) },
                supportingContent = {
                    if (email.main) {
                        Text(stringResource(R.string.emails_main))
                    }
                },
                trailingContent = {
                    if (!email.verified) {
                        Button(onClick = {
                            viewModel.updateUnverifiedEmail(email.email)
                            showVerifyEmail = true
                        }) {
                            Text(stringResource(R.string.emails_verify))
                        }
                    }
                },
                modifier = Modifier.animateItem()
            )
        }

        if (unverifiedCount > 0) {
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = pluralStringResource(
                                R.plurals.emails_help_random_code,
                                unverifiedCount,
                                unverifiedCount
                            ),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    if (showAddEmail) {
        AddEmailDialog(
            email = viewModel.newEmail,
            onUpdate = viewModel::updateNewEmail,
            onAccept = viewModel::addNewEmail,
            onClose = { showAddEmail = false }
        )
    }
    if (showVerifyEmail) {
        VerifyEmailDialog(
            randomCode = viewModel.randomCode,
            onUpdate = viewModel::updateRandomCode,
            onAccept = viewModel::verifyEmail,
            onClose = { showVerifyEmail = false }
        )
    }

    LaunchedEffect(mainActionClicker) {
        mainActionClicker.setOnClickHandler { showAddEmail = true }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun EmailsScreenPreview(
    @PreviewParameter(EmailsPreviewProvider::class)
    viewModel: EmailsViewModel
) {
    EmailsScreen(
        mainActionClicker = Clicker(),
        viewModel = viewModel
    )
}

private class EmailsPreviewProvider : PreviewParameterProvider<EmailsViewModel> {
    override val values = sequenceOf(
        EmailsViewModel(
            state = SavedStateHandle(),
            eventBus = FakeEventBus(),
            storeResolver = FakeStoreResolver(),
            apiResolver = FakeApiResolver()
        )
    )
}

@Composable
private fun AddEmailDialog(
    email: String,
    onUpdate: (String) -> Unit,
    onAccept: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.TwoTone.AlternateEmail, null)
        },
        title = {
            Text(stringResource(R.string.emails_add_dialog_title))
        },
        text = {
            TextField(
                value = email,
                placeholder = { Text(stringResource(R.string.emails_add_dialog_placeholder)) },
                onValueChange = onUpdate
            )
        },
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                enabled = email.isNotBlank(),
                onClick = {
                    onAccept()
                    onClose()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview
@Composable
fun AddEmailDialogPreview() {
    AddEmailDialog(
        email = "",
        onUpdate = {},
        onAccept = {},
        onClose = {}
    )
}

@Composable
private fun VerifyEmailDialog(
    randomCode: String,
    onUpdate: (String) -> Unit,
    onAccept: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.TwoTone.Password, null)
        },
        title = {
            Text(stringResource(R.string.account_random_code))
        },
        text = {
            TextField(
                value = randomCode,
                placeholder = { Text(stringResource(R.string.account_random_code_placeholder)) },
                onValueChange = onUpdate
            )
        },
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                enabled = randomCode.isNotBlank(),
                onClick = {
                    onAccept()
                    onClose()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
