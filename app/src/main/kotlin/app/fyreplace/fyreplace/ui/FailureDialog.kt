package app.fyreplace.fyreplace.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.events.Event

@Composable
fun FailureDialog(failure: Event.Failure, dismiss: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(Icons.TwoTone.Error, null)
        },
        title = {
            Text(stringResource(failure.title))
        },
        text = {
            Text(stringResource(failure.message))
        },
        onDismissRequest = dismiss,
        confirmButton = {
            TextButton(onClick = dismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
