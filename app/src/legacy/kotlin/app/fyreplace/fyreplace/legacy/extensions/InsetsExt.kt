package app.fyreplace.fyreplace.legacy.extensions

import androidx.core.graphics.Insets

operator fun Insets.plus(other: Insets) = Insets.of(
    left + other.left,
    top + other.top,
    right + other.right,
    bottom + other.bottom
)
