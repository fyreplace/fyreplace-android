package app.fyreplace.fyreplace.ui.views.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fyreplace.fyreplace.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Preference(
    title: String,
    summary: String? = null,
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .combinedClickable(enabled = enabled, onClick = onClick, onLongClick = onLongClick)
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .padding(
                horizontal = dimensionResource(R.dimen.spacing_large),
                vertical = dimensionResource(R.dimen.spacing_medium)
            )
            .then(modifier)
    ) {
        icon()

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreferencePreview() {
    Preference(
        title = "Title",
        summary = "Summary",
        icon = {
            Icon(Icons.TwoTone.Image, null)
        }
    )
}
