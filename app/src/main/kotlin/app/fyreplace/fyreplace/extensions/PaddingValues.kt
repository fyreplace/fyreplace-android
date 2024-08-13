package app.fyreplace.fyreplace.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

val PaddingValues.start
    @Composable
    @Stable
    get() = calculateStartPadding(LocalLayoutDirection.current)

val PaddingValues.end
    @Composable
    @Stable
    get() = calculateEndPadding(
        LocalLayoutDirection.current
    )

val PaddingValues.top
    @Composable
    @Stable
    get() = calculateTopPadding()

val PaddingValues.bottom
    @Composable
    @Stable
    get() = calculateBottomPadding()

@Composable
@Stable
operator fun PaddingValues.plus(other: PaddingValues) = PaddingValues(
    start = start + other.start,
    top = top + other.top,
    end = end + other.end,
    bottom = bottom + other.bottom
)

@Composable
@Stable
operator fun PaddingValues.minus(other: PaddingValues) = PaddingValues(
    start = start - other.start,
    top = top - other.top,
    end = end - other.end,
    bottom = bottom - other.bottom
)

@Composable
@Stable
fun PaddingValues.modify(
    start: Dp = this.start,
    top: Dp = this.top,
    end: Dp = this.end,
    bottom: Dp = this.bottom
) = PaddingValues(start, top, end, bottom)
