package app.fyreplace.fyreplace.ui.views.settings

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun LinkListItem(
    title: String,
    uri: String,
    icon: ImageVector
) {
    val uriHandler = LocalUriHandler.current
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(uri) },
        leadingContent = { Icon(icon, null) },
        modifier = Modifier.clickable(onClick = { uriHandler.openUri(uri) })
    )
}
