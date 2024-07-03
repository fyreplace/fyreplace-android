package app.fyreplace.fyreplace.ui.views.bars

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TopBar(
    destinations: List<Destination>,
    selectedDestination: Destination?,
    onClickDestination: (Destination) -> Unit
) = if (destinations.isNotEmpty()) {
    CenterAlignedTopAppBar(title = {
        SharedTransitionLayout {
            AnimatedContent(destinations, label = "Top bar segments") {
                SegmentedChoice(
                    destinations = it,
                    selectedDestination = selectedDestination,
                    visibilityScope = this,
                    onClick = onClickDestination,
                )
            }
        }
    })
} else {
    TopAppBar(title = {
        if (selectedDestination != null) {
            Text(stringResource(selectedDestination.labelRes))
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.SegmentedChoice(
    destinations: List<Destination>,
    selectedDestination: Destination?,
    visibilityScope: AnimatedVisibilityScope,
    onClick: (Destination) -> Unit
) = SingleChoiceSegmentedButtonRow(
    modifier = Modifier.sharedElement(
        rememberSharedContentState(key = "segments"),
        visibilityScope
    )
) {
    for ((i, destination) in destinations.withIndex()) {
        val selected = destination == selectedDestination

        SegmentedButton(
            selected = selected,
            shape = SegmentedButtonDefaults.itemShape(index = i, count = destinations.size),
            icon = {
                if (booleanResource(R.bool.cramped_width)) {
                    SegmentedButtonDefaults.Icon(active = selected)
                } else {
                    Icon(destination, selected = selected)
                }
            },
            onClick = { onClick(destination) },
            modifier = Modifier.testTag("navigation:$destination")
        ) {
            Text(stringResource(destination.labelRes))
        }
    }
}
