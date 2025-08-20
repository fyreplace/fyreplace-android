package app.fyreplace.fyreplace.ui.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import app.fyreplace.fyreplace.R

@Composable
fun Section(header: String, content: @Composable () -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Text(
            text = header,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_medium))
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
        content()
    }
}
