package app.fyreplace.fyreplace.ui.views.account

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import app.fyreplace.fyreplace.R

@Composable
fun RandomCodeInput(
    randomCode: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = randomCode,
        label = { Text(stringResource(R.string.account_random_code)) },
        placeholder = { Text(stringResource(R.string.account_random_code_placeholder)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        onValueChange = onValueChange,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun RandomCodeInputPreview() {
    RandomCodeInput(
        randomCode = "",
        onValueChange = {},
        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
    )
}
