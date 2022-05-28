package app.fyreplace.fyreplace.ui

fun Int.translucent() = (this and 0x00FFFFFF) or 0x7F000000
