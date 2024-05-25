package app.fyreplace.fyreplace.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.theme.AppTheme
import app.fyreplace.fyreplace.ui.views.navigation.Destination

@Composable
fun FeedScreen() {
    Box {
        Text(
            stringResource(R.string.main_destination_feed),
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("screen:${Destination.FEED}")
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun FeedScreenPreview() {
    AppTheme {
        FeedScreen()
    }
}
