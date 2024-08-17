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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon
import app.fyreplace.fyreplace.viewmodels.bar.TopBarViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TopBar(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    onClickDestination: (Destination.Singleton) -> Unit
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
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination.Singleton?,
    visibilityScope: AnimatedVisibilityScope,
    onClick: (Destination.Singleton) -> Unit,
    viewModel: TopBarViewModel = hiltViewModel()
) = SingleChoiceSegmentedButtonRow(
    modifier = Modifier.sharedElement(
        rememberSharedContentState(key = "segments"),
        visibilityScope
    )
) {
    val isWaitingForRandomCode by viewModel.isWaitingForRandomCode.collectAsStateWithLifecycle()

    for ((i, destination) in destinations.withIndex()) {
        val selected = destination == selectedDestination

        SegmentedButton(
            selected = selected,
            enabled = !isWaitingForRandomCode,
            shape = SegmentedButtonDefaults.itemShape(index = i, count = destinations.size),
            colors = SegmentedButtonDefaults.patchedColors(),
            icon = {
                if (booleanResource(R.bool.cramped_width)) {
                    SegmentedButtonDefaults.Icon(active = selected)
                } else {
                    Icon(destination, active = selected)
                }
            },
            onClick = { onClick(destination) }
        ) {
            Text(
                text = stringResource(destination.labelRes),
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedButtonDefaults.patchedColors() = colors().copy(
    disabledInactiveContentColor = colors().disabledActiveContentColor,
    disabledInactiveBorderColor = colors().disabledActiveBorderColor
)
