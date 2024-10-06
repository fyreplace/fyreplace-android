package app.fyreplace.fyreplace.ui.views

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fyreplace.api.data.User
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.extensions.composeColor
import app.fyreplace.fyreplace.fakes.placeholder
import coil.compose.AsyncImage

@Composable
fun Avatar(user: User?, modifier: Modifier = Modifier) {
    val fallback = rememberVectorPainter(Icons.Filled.AccountCircle)
    val error = rememberVectorPainter(Icons.TwoTone.Error)
    val hasAvatar = !user?.avatar.isNullOrEmpty()
    AsyncImage(
        model = if (hasAvatar) user.avatar else null,
        placeholder = fallback,
        error = error,
        fallback = fallback,
        contentDescription = user?.username,
        contentScale = ContentScale.Crop,
        colorFilter = when {
            hasAvatar -> null
            user != null -> ColorFilter.tint(user.tint.composeColor)
            else -> ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        },
        modifier = modifier
            .clip(CircleShape)
            .scale(if (hasAvatar) 1f else 1.2f)
    )
}

@Preview(showBackground = true)
@Composable
fun AvatarPreview() {
    Avatar(
        user = User.placeholder,
        modifier = Modifier
            .padding(dimensionResource(R.dimen.spacing_medium))
            .size(128.dp)
    )
}
