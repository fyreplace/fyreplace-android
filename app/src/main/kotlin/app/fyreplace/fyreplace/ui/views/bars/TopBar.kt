package app.fyreplace.fyreplace.ui.views.bars

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TopBar(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination?,
    enabled: Boolean,
    onClickDestination: (Destination.Singleton) -> Unit,
    onBack: (() -> Unit)? = null
) {
    @Composable
    fun BackButton() {
        AnimatedVisibility(
            visible = onBack != null,
            enter = slideInHorizontally() + expandHorizontally(),
            exit = slideOutHorizontally() + shrinkHorizontally()
        ) {
            IconButton(onClick = { onBack?.invoke() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
        }
    }

    if (destinations.isNotEmpty()) {
        CenterAlignedTopAppBar(
            title = {
                SharedTransitionLayout {
                    AnimatedContent(destinations, label = "Top bar segments") {
                        SegmentedChoice(
                            destinations = it,
                            selectedDestination = selectedDestination,
                            enabled = enabled,
                            visibilityScope = this,
                            onClick = onClickDestination,
                        )
                    }
                }
            },
            navigationIcon = { BackButton() }
        )
    } else {
        TopAppBar(
            title = {
                if (selectedDestination != null) {
                    Text(stringResource(selectedDestination.labelRes))
                }
            },
            navigationIcon = { BackButton() }
        )
    }
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar(
        destinations = emptyList(),
        selectedDestination = Destination.Feed,
        enabled = true,
        onClickDestination = {}
    )
}

@Preview
@Composable
fun TopBarWithButtonsPreview() {
    TopBar(
        destinations = listOf(Destination.Login(), Destination.Register()),
        selectedDestination = Destination.Login(),
        enabled = true,
        onClickDestination = {}
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.SegmentedChoice(
    destinations: List<Destination.Singleton>,
    selectedDestination: Destination?,
    enabled: Boolean,
    visibilityScope: AnimatedVisibilityScope,
    onClick: (Destination.Singleton) -> Unit
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
            enabled = enabled,
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

@Composable
private fun SegmentedButtonDefaults.patchedColors() = colors().copy(
    disabledInactiveContentColor = colors().disabledActiveContentColor,
    disabledInactiveBorderColor = colors().disabledActiveBorderColor
)
