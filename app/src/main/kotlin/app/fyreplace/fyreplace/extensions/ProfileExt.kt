package app.fyreplace.fyreplace.extensions

import app.fyreplace.protos.Profile

val Profile.isAvailable get() = !isBanned && username.isNotEmpty()
