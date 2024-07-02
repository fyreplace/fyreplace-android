package app.fyreplace.fyreplace.ui.views.bars

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    destinations: List<Destination>,
    selectedDestination: Destination?,
    onClickDestination: (Destination) -> Unit
) = if (destinations.isNotEmpty()) {
    CenterAlignedTopAppBar(title = {
        SegmentedChoice(
            destinations = destinations,
            selectedDestination = selectedDestination,
            onClick = onClickDestination,
        )
    })
} else {
    TopAppBar(title = {
        if (selectedDestination != null) {
            Text(stringResource(selectedDestination.labelRes))
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedChoice(
    destinations: List<Destination>,
    selectedDestination: Destination?,
    onClick: (Destination) -> Unit
) = SingleChoiceSegmentedButtonRow {
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
