package app.fyreplace.fyreplace.ui.views.bars

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.fyreplace.fyreplace.R
import app.fyreplace.fyreplace.ui.views.navigation.Destination
import app.fyreplace.fyreplace.ui.views.navigation.Icon
import app.fyreplace.fyreplace.ui.views.navigation.asDestination
import app.fyreplace.fyreplace.ui.views.navigation.isAt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    destinations: List<Destination> = emptyList(),
    onClickDestination: (Destination) -> Unit
) {
    val entry by navController.currentBackStackEntryAsState()
    val currentDestination = entry?.asDestination()

    if (destinations.isNotEmpty()) {
        CenterAlignedTopAppBar(title = {
            SegmentedChoice(
                navController = navController,
                destinations = destinations,
                onClick = onClickDestination,
            )
        })
    } else {
        TopAppBar(title = {
            if (currentDestination != null) {
                Text(stringResource(currentDestination.labelRes))
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedChoice(
    navController: NavController,
    destinations: List<Destination>,
    onClick: (Destination) -> Unit
) {
    val entry by navController.currentBackStackEntryAsState()

    SingleChoiceSegmentedButtonRow {
        for ((i, destination) in destinations.withIndex()) {
            val selected = entry.isAt(destination)

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
}
