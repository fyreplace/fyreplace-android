package app.fyreplace.fyreplace.ui.views

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import app.fyreplace.api.data.User
import app.fyreplace.fyreplace.extensions.composeColor
import coil.compose.AsyncImage

@Composable
fun Avatar(
    user: User?,
    tinted: Boolean,
    modifier: Modifier = Modifier,
    size: Dp
) {
    val fallback = rememberVectorPainter(Icons.TwoTone.AccountCircle)
    val error = rememberVectorPainter(Icons.TwoTone.Error)
    val hasAvatar = !user?.avatar.isNullOrEmpty()
    AsyncImage(
        model = if (hasAvatar) user?.avatar else null,
        placeholder = fallback,
        error = error,
        fallback = fallback,
        contentDescription = user?.username,
        contentScale = ContentScale.Crop,
        colorFilter = when {
            !tinted || hasAvatar || user == null -> null
            else -> ColorFilter.tint(user.tint.composeColor)
        },
        modifier = modifier
            .size(size)
            .scale(if (hasAvatar) 1f else 1.2f)
            .clip(CircleShape)
    )
}
