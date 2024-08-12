package app.fyreplace.fyreplace.ui.views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun SmallCircularProgressIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        strokeCap = StrokeCap.Round,
        strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 2,
        modifier = Modifier
            .size(24.dp)
            .then(modifier)
    )
}
