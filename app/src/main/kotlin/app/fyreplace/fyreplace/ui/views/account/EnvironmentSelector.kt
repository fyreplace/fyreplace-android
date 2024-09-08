package app.fyreplace.fyreplace.ui.views.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cloud
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import app.fyreplace.fyreplace.BuildConfig
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.protos.Environment

@Composable
fun EnvironmentSelector(
    environment: Environment,
    onEnvironmentChange: (Environment) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDialog = true },
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(Icons.TwoTone.Cloud, contentDescription = null)
        Text(
            name(environment),
            modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_small))
        )
    }

    if (showDialog) {
        SelectorDialog(
            environment = environment,
            onSelect = onEnvironmentChange,
            onClose = { showDialog = false }
        )
    }
}

@Composable
private fun SelectorDialog(
    environment: Environment,
    onSelect: (Environment) -> Unit,
    onClose: () -> Unit
) {
    var selectedEnvironment by rememberSaveable { mutableStateOf(environment) }

    AlertDialog(
        icon = {
            Icon(Icons.TwoTone.Cloud, null)
        },
        title = {
            Text(stringResource(R.string.account_environment_dialog_title))
        },
        text = {
            Column {
                Text(stringResource(R.string.account_environment_dialog_text))

                for (env in Environment.entries.filter { url(it).isNotEmpty() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val onClick = { selectedEnvironment = env }
                        RadioButton(
                            selected = env == selectedEnvironment,
                            onClick = onClick
                        )
                        Text(
                            buildAnnotatedString {
                                withLink(LinkAnnotation.Clickable("name") { onClick() }) {
                                    append(name(env))

                                    if (env == BuildConfig.ENVIRONMENT_DEFAULT) {
                                        append(" ")
                                        append(stringResource(R.string.account_environment_default))
                                    }
                                }
                            },
                            modifier = Modifier.focusProperties { canFocus = false }
                        )
                    }
                }
            }
        },
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                onClick = {
                    onSelect(selectedEnvironment)
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
fun SelectorDialogPreview() = SelectorDialog(Environment.LOCAL, {}, {})

@Composable
@ReadOnlyComposable
private fun name(env: Environment) = stringResource(
    when (env) {
        Environment.MAIN -> R.string.account_environment_main
        Environment.DEV -> R.string.account_environment_dev
        Environment.LOCAL -> R.string.account_environment_local
        Environment.UNSPECIFIED,
        Environment.UNRECOGNIZED -> R.string.loading
    }
)

@Composable
@ReadOnlyComposable
private fun url(env: Environment) = stringResource(
    when (env) {
        Environment.MAIN -> R.string.api_url_main
        Environment.DEV -> R.string.api_url_dev
        Environment.LOCAL -> R.string.api_url_local
        Environment.UNSPECIFIED,
        Environment.UNRECOGNIZED -> R.string.empty
    }
)
