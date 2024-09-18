package app.fyreplace.fyreplace.extensions

import app.fyreplace.api.data.Color

val Color.composeColor
    get() = androidx.compose.ui.graphics.Color(r, g, b)
