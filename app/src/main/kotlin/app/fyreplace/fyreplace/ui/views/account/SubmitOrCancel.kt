package app.fyreplace.fyreplace.ui.views.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.SmallCircularProgressIndicator

@Composable
fun SubmitOrCancel(
    submitLabel: String,
    canSubmit: Boolean,
    canCancel: Boolean,
    isLoading: Boolean,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Button(
            enabled = canSubmit,
            onClick = onSubmit
        ) {
            Box {
                Text(
                    text = submitLabel,
                    color = if (isLoading) Color.Transparent else Color.Unspecified,
                    maxLines = 1
                )

                if (isLoading) {
                    SmallCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        AnimatedVisibility(
            visible = canCancel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FilledTonalButton(
                enabled = !isLoading,
                onClick = onCancel
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubmitOrCancelPreview() {
    SubmitOrCancel(
        submitLabel = "Submit",
        canSubmit = true,
        canCancel = true,
        isLoading = false,
        onSubmit = {},
        onCancel = {},
        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
    )
}
