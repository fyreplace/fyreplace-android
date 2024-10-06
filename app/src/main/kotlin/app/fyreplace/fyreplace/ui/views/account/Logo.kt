package app.fyreplace.fyreplace.ui.views.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.fyreplace.fyreplace.R

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Image(
        imageVector = ImageVector.vectorResource(R.drawable.logo),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        contentDescription = null,
        modifier = Modifier
            .padding(vertical = dimensionResource(R.dimen.spacing_huge))
            .size(96.dp)
            .then(modifier)
    )
}

@Preview
@Composable
fun LogoPreview() {
    Logo()
}
